package com.bites.tools;

import com.bites.dto.response.ImageUploadResponse;
import com.bites.entity.Category;
import com.bites.entity.MenuItem;
import com.bites.repository.CategoryRepository;
import com.bites.repository.MenuItemRepository;
import com.bites.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;

/**
 * ONE-OFF seeding tool. For every Category / MenuItem row whose image_url is
 * still NULL, looks for an image file on disk whose name (without extension)
 * matches the row's `name` exactly (case-insensitive), uploads it through
 * the app's existing ImageUploadService (same path AdminUploadController
 * uses), and saves the returned Cloudinary URL back onto that row.
 *
 * Safe to re-run — rows that already have an image_url are skipped, so
 * nothing gets re-uploaded or overwritten.
 *
 * IMPORTANT: this runs automatically on every app startup (no profile
 * gating) — that's intentional for quick one-time use, but it means you
 * MUST delete this class (and the ImageSeedRunner.java file) once seeding
 * is done, otherwise it'll keep scanning + hitting Cloudinary/the DB on
 * every future boot.
 *
 * ---------------------------------------------------------------------
 * HOW TO RUN
 * ---------------------------------------------------------------------
 * 1. Add to application.yml (point these at your actual image folders):
 *
 * app:
 * seed:
 * category-image-dir: C:/path/to/category-images
 * menu-item-image-dir: C:/path/to/menu-item-images
 *
 * 2. Just run the app normally:
 * mvn spring-boot:run
 *
 * 3. Watch the console — it logs every upload, skip, and the final URL.
 *
 * 4. DELETE this file once done.
 * ---------------------------------------------------------------------
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageSeedRunner implements CommandLineRunner {

    private static final String[] EXTENSIONS = { "jpg", "jpeg", "png", "webp" };

    private final ImageUploadService imageUploadService;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    @Value("${app.seed.category-image-dir:images/categories}")
    private String categoryImageDir;

    @Value("${app.seed.menu-item-image-dir:images/menu-items}")
    private String menuItemImageDir;

    @Override
    public void run(String... args) {
        log.info("=== Image seeding started ===");
        seedCategories();
        seedMenuItems();
        log.info("=== Image seeding finished ===");
    }

    private void seedCategories() {
        List<Category> categories = categoryRepository.findAll();
        log.info("Checking {} categories...", categories.size());

        for (Category category : categories) {
            if (category.getImageUrl() != null) {
                continue;
            }
            File imageFile = findImageFile(categoryImageDir, category.getName());
            if (imageFile == null) {
                log.warn("[SKIP] Category \"{}\" — no matching file found in {}", category.getName(), categoryImageDir);
                continue;
            }
            String url = uploadViaService(imageFile);
            category.setImageUrl(url);
            categoryRepository.save(category);
            log.info("[OK] Category \"{}\" -> {}", category.getName(), url);
        }
    }

    private void seedMenuItems() {
        List<MenuItem> menuItems = menuItemRepository.findAll();
        log.info("Checking {} menu items...", menuItems.size());

        for (MenuItem item : menuItems) {
            if (item.getImageUrl() != null) {
                continue;
            }
            File imageFile = findImageFile(menuItemImageDir, item.getName());
            if (imageFile == null) {
                log.warn("[SKIP] Menu item \"{}\" — no matching file found in {}", item.getName(), menuItemImageDir);
                continue;
            }
            String url = uploadViaService(imageFile);
            item.setImageUrl(url);
            menuItemRepository.save(item);
            log.info("[OK] Menu item \"{}\" -> {}", item.getName(), url);
        }
    }

    // Looks in `dir` for a file named exactly like `name` (case-insensitive,
    // ignoring the extension) — e.g. name "Chicken 65" matches
    // "Chicken 65.jpg", "chicken 65.JPG", etc.
    private File findImageFile(String dir, String name) {
        File folder = new File(dir);
        File[] files = folder.listFiles();
        if (files == null) {
            log.warn("Image directory not found or not readable: {}", dir);
            return null;
        }
        for (File file : files) {
            String baseName = stripExtension(file.getName());
            if (baseName.equalsIgnoreCase(name)) {
                return file;
            }
        }
        return null;
    }

    private String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
    }

    private String uploadViaService(File file) {
        MultipartFile multipartFile = new FileMultipartFile(file);
        ImageUploadResponse response = imageUploadService.upload(multipartFile);
        return response.url();
    }

    // Minimal MultipartFile wrapper around a local File, so a plain disk file
    // can be passed into ImageUploadService.upload(MultipartFile) without
    // needing an actual HTTP request. Only the methods ImageUploadServiceImpl
    // actually calls are meaningfully implemented.
    private static class FileMultipartFile implements MultipartFile {

        private final File file;
        private final String contentType;

        FileMultipartFile(File file) {
            this.file = file;
            this.contentType = resolveContentType(file);
        }

        private static String resolveContentType(File file) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".png")) {
                return MimeTypeUtils.IMAGE_PNG_VALUE;
            }
            if (name.endsWith(".webp")) {
                return "image/webp";
            }
            // covers .jpg and .jpeg
            return MimeTypeUtils.IMAGE_JPEG_VALUE;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return file.getName();
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return file.length() == 0;
        }

        @Override
        public long getSize() {
            return file.length();
        }

        @Override
        public byte[] getBytes() throws IOException {
            return Files.readAllBytes(file.toPath());
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            Files.copy(file.toPath(), dest.toPath());
        }
    }
}
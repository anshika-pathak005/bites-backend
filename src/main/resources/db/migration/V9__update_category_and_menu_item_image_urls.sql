-- =========================================================
-- V9: Update image_url for category and menu_item tables
-- (image_url was inserted as NULL in V7 and V8; now backfilling
-- the real Cloudinary URLs)
-- =========================================================

-- ---------------------------------------------------------
-- category (9 rows)
-- ---------------------------------------------------------
UPDATE category SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516241/bites/mcfzgkdqzfrg5ubts5tp.jpg'  WHERE id = 1; -- Pizza
UPDATE category SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516253/bites/mnfehpt1df3u0ifndz2y.jpg'  WHERE id = 2; -- Burgers
UPDATE category SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516270/bites/ujzzdp4mt9ojia7yaiby.jpg'  WHERE id = 3; -- Sandwiches
UPDATE category SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516279/bites/ubjhbrbwxbiyk8aoaho5.jpg'  WHERE id = 4; -- Momos
UPDATE category SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516305/bites/i5ifltnmodiuj3w4faqo.jpg'  WHERE id = 5; -- Rolls
UPDATE category SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516322/bites/jqls8bpxfcepcnugry4u.jpg'  WHERE id = 6; -- Pasta & Noodles
UPDATE category SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516370/bites/lmepde3iocrsn14icqu3.jpg'  WHERE id = 7; -- Sides & Snacks
UPDATE category SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516384/bites/zmvcvhsj3uzpz8gevvcz.avif' WHERE id = 8; -- Desserts
UPDATE category SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516403/bites/rsnrwdltbi2pm1exuwvw.avif' WHERE id = 9; -- Beverages

-- ---------------------------------------------------------
-- menu_item (62 rows)
-- ---------------------------------------------------------
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516440/bites/uay175vgdpwcsf5xtqbw.avif' WHERE id = 1;  -- Margherita Pizzaa
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516476/bites/egjdk3a9hnew10vuoyac.avif' WHERE id = 2;  -- Farmhouse Pizza
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516500/bites/sdkkzk6tkvzsrnl1rscv.avif' WHERE id = 3;  -- Chicken Tikka Pizza
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516534/bites/fpnds0byaxdnth28s40b.avif' WHERE id = 4;  -- Pepperoni Pizza
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516570/bites/nm6rk9vp1suyw2nq7wxp.avif' WHERE id = 5;  -- Mexican Green Wave Pizza
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516596/bites/qwc4rvbop5ihvaxorlyh.avif' WHERE id = 6;  -- Cheese Burst Pizza
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516612/bites/hsghsva1pgqpz3ftka9h.avif' WHERE id = 7;  -- BBQ Chicken Pizza
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516628/bites/puxny8btpel1ltkfleuo.avif' WHERE id = 8;  -- Aloo Tikki Burger
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516649/bites/ehzzr9pvx84igkawu8gl.avif' WHERE id = 9;  -- Chicken Zinger Burger
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516669/bites/sl4c0y8pn2wgqqebzqyw.avif' WHERE id = 10; -- Classic Veg Burger
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516682/bites/c0qvwn1xrxxtl6rsqp2p.avif' WHERE id = 11; -- Double Cheese Veg Burger
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516691/bites/iy56c5ad5vtoujx9mkk0.avif' WHERE id = 12; -- Grilled Chicken Burger
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516701/bites/og7ml22td5nmybbv8zo0.avif' WHERE id = 13; -- Paneer Tikka Burger
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516705/bites/efq7kdwq63yhnsrjxub8.avif' WHERE id = 14; -- Bombay Masala Sandwich
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516710/bites/db3qstclkjqi1udjk6af.avif' WHERE id = 15; -- Cheese Corn Sandwich
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516717/bites/qvvn61wjt5mh4w3ykmnl.avif' WHERE id = 16; -- Chicken Club Sandwich
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516720/bites/xa7onfrj1y4xgaehej60.avif' WHERE id = 17; -- Club Sandwich
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516723/bites/ewtfnm2suaz1b4opzuvk.avif' WHERE id = 18; -- Paneer Tikka Sandwich
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516725/bites/qdovvhqeimec5tmksf2m.avif' WHERE id = 19; -- Veg Grilled Sandwich
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516728/bites/uv6qkrsx8lnjfgtwwkyf.avif' WHERE id = 20; -- Cheese Corn Momos
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516735/bites/fan3fq1ybnmwhjfuzq47.jpg'  WHERE id = 21; -- Chicken Fried Momos
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516754/bites/igz0rpfc6dxrcc2vrtuu.jpg'  WHERE id = 22; -- Chicken Momos
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516763/bites/zaeatqkdspuei0koe8as.avif' WHERE id = 23; -- Chicken Steamed Momos
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516775/bites/engiizdror82od28ywia.jpg'  WHERE id = 24; -- Paneer Momos
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516779/bites/heyhkecp1rbkmaevj1ns.jpg'  WHERE id = 25; -- Veg Momos
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516783/bites/ofvojbajomw6neh0nixn.jpg'  WHERE id = 26; -- Chicken Egg Double Roll
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516785/bites/cs7knjcr686zqip1ydg6.jpg'  WHERE id = 27; -- Chicken Egg Single Roll
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516790/bites/pv2ozgjw6v408ow05f8s.jpg'  WHERE id = 28; -- Chinese Veg Roll
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516794/bites/t4twx0anvigaxcb97rj5.jpg'  WHERE id = 29; -- Paneer Roll
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516806/bites/soctiohjs2g7w7havn8e.jpg'  WHERE id = 30; -- Plain Special Roll
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516813/bites/cjsxj5yxmbrewn14zsqn.avif' WHERE id = 31; -- Chicken Alfredo Pasta
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516817/bites/wqgjcqkibx9l2sbmd04g.avif' WHERE id = 32; -- Chicken Hakka Noodles
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516843/bites/sxigzg91uas9vtszgeoy.avif' WHERE id = 33; -- Mixed Sauce Pasta
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516847/bites/h95s6rsely5jw487uu7q.avif' WHERE id = 34; -- Peri Peri Pasta
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516851/bites/tx3lawgetdrtxf61afab.avif' WHERE id = 35; -- Red Sauce Pasta
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516855/bites/dxtxikwdwh02fgxyavur.avif' WHERE id = 36; -- Schezwan Veg Noodles
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516859/bites/kygocmarav6obmldblng.avif' WHERE id = 37; -- Singapore Noodles
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516863/bites/rql9fpfontnwhesczxcy.avif' WHERE id = 38; -- Veg Hakka Noodles
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516867/bites/ffeeinyntywaprytxozz.avif' WHERE id = 39; -- White Sauce Pasta
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516872/bites/jzvsoxhyb0t8m66j6pe0.avif' WHERE id = 40; -- Cheesy Loaded Fries
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516875/bites/osmshgb9arbtity3eaje.avif' WHERE id = 41; -- Chicken Nuggets
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516886/bites/trn9bff4xdkbkgpfetip.avif' WHERE id = 42; -- Chicken Wings
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516891/bites/sqqldpulmertf0qjsaai.avif' WHERE id = 43; -- Classic French Fries
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516896/bites/uhgrfafn8ylpbt7jhom0.avif' WHERE id = 44; -- Onion Rings
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516900/bites/pzsunvhltmnlx4h6ojc8.avif' WHERE id = 45; -- Peri Peri Fries
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516904/bites/bwi1ztdeabn4lundduxt.avif' WHERE id = 46; -- Brownie with Ice Cream
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516912/bites/d7lljh7hdebet5cve9mh.avif' WHERE id = 47; -- Butterscotch Ice Cream
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516917/bites/mcbxa70te1ansavwcu85.avif' WHERE id = 48; -- Chocolate Brownie
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516921/bites/vmosydwc8vr1rhqxgjj7.avif' WHERE id = 49; -- Chocolate Ice Cream
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516925/bites/vlmiooxyhbsbpeq0pa6i.avif' WHERE id = 50; -- Chocolate Truffle Pastry
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516929/bites/cihua3j8s1yuln9e8mjz.avif' WHERE id = 51; -- Vanilla Ice Cream
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516933/bites/dbtbbhz20bqcqymw3wvl.avif' WHERE id = 52; -- Coca Cola
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516935/bites/dzayrzmnag1wgophqgkt.avif' WHERE id = 53; -- Cold Coffee
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516937/bites/rtntaqfj9ereoraytwxp.avif' WHERE id = 54; -- Fresh Lime Soda
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516941/bites/xrqv35ex67gzazd80qqj.avif' WHERE id = 55; -- Fresh Orange Juice
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516945/bites/eyjkugp9knzqiq7gpkix.avif' WHERE id = 56; -- Hot Cappuccino
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516951/bites/xttjrzlsrop2upsowlu5.avif' WHERE id = 57; -- Iced Tea
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516955/bites/vqfvkowdxastqzsi3m9j.avif' WHERE id = 58; -- Mango Shake
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516959/bites/q8sdxpnypmbwgf7cdjvh.avif' WHERE id = 59; -- Masala Chai
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516962/bites/uzt8guthb7ztgd8besjx.avif' WHERE id = 60; -- Oreo Milkshake
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516965/bites/kpfpwx85kyadwl2umulx.avif' WHERE id = 61; -- Pepsi
UPDATE menu_item SET image_url = 'https://res.cloudinary.com/do0itnacu/image/upload/v1786516970/bites/k5rrrrxwdgga3wm7fyak.avif' WHERE id = 62; -- Soda Water

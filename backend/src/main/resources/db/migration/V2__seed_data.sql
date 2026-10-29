INSERT INTO users (email, password_hash, full_name, phone, role) VALUES
('admin@tripcraft.test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TripCraft Admin', '+919900000001', 'ROLE_ADMIN'),
('tourist@tripcraft.test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Aarav Tourist', '+919900000002', 'ROLE_TOURIST');

INSERT INTO user_interests (user_id, interest) VALUES
(2,'NATURE'),(2,'FOOD'),(2,'HISTORY');

INSERT INTO destinations (name,slug,state,description,best_season,hero_image_url,latitude,longitude) VALUES
('Coorg','coorg','Karnataka','Misty coffee plantations, waterfalls and forest trails in the Western Ghats.','October to March','https://images.unsplash.com/photo-1593693397690-362cb9666fc2?auto=format&fit=crop&w=1600&q=80',12.3375,75.8069),
('Jaipur','jaipur','Rajasthan','Royal forts, colorful bazaars and celebrated Rajasthani cuisine.','October to March','https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1600&q=80',26.9124,75.7873),
('Munnar','munnar','Kerala','Emerald tea hills, cool valleys and wildlife around Eravikulam.','September to May','https://images.unsplash.com/photo-1593693397690-362cb9666fc2?auto=format&fit=crop&w=1600&q=80',10.0889,77.0595),
('Goa','goa','Goa','Sunset beaches, Portuguese heritage, seafood and easygoing coastal life.','November to February','https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=1600&q=80',15.2993,74.1240),
('Varanasi','varanasi','Uttar Pradesh','Ancient ghats, temple rituals, silk craft and an unforgettable riverside culture.','October to March','https://images.unsplash.com/photo-1561361058-c24cecae35ca?auto=format&fit=crop&w=1600&q=80',25.3176,82.9739);

INSERT INTO places (destination_id,name,slug,description,suitable_for,best_season,difficulty,price,duration_minutes,rating,image_url) VALUES
(1,'Abbey Falls','coorg-abbey-falls','A lush waterfall surrounded by coffee and spice plantations.','families, adults, seniors','June to March','EASY',150,120,4.60,'https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=900&q=80'),
(1,'Raja Seat','coorg-raja-seat','Sunset viewpoint across rolling Kodagu valleys.','families, adults, seniors','October to March','EASY',50,90,4.50,'https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=900&q=80'),
(1,'Dubare Elephant Camp','coorg-dubare-elephant-camp','Riverside elephant camp with guided nature activities.','families, adults','October to May','EASY',450,180,4.40,'https://images.unsplash.com/photo-1557050543-4d5f4e07ef46?auto=format&fit=crop&w=900&q=80'),
(1,'Mandalpatti Jeep Trail','coorg-mandalpatti','A dramatic jeep ride to a high-altitude cloud viewpoint.','adults, adventure','October to May','MODERATE',900,240,4.70,'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=80'),
(1,'Coffee Estate Walk','coorg-coffee-estate','A plantation walk with tasting and a local breakfast.','families, adults, seniors','October to March','EASY',600,150,4.80,'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=80'),
(1,'Nagarhole Safari','coorg-nagarhole-safari','Early morning wildlife safari on the forest edge.','adults, adventure','October to May','MODERATE',1400,300,4.60,'https://images.unsplash.com/photo-1535338454770-8be927b5a00b?auto=format&fit=crop&w=900&q=80'),
(2,'Amber Fort','jaipur-amber-fort','Hilltop Rajput fort with courtyards, mirrors and sweeping views.','families, adults, seniors','October to March','EASY',550,180,4.80,'https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=900&q=80'),
(2,'City Palace','jaipur-city-palace','Living royal complex with galleries and ornate architecture.','families, adults, seniors','October to March','EASY',500,150,4.70,'https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=900&q=80'),
(2,'Hawa Mahal','jaipur-hawa-mahal','The iconic pink sandstone palace of winds.','families, adults, seniors','October to March','EASY',200,75,4.60,'https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=900&q=80'),
(2,'Jantar Mantar','jaipur-jantar-mantar','UNESCO observatory with monumental astronomical instruments.','families, adults','October to March','EASY',250,90,4.50,'https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=900&q=80'),
(2,'Nahargarh Sunset','jaipur-nahargarh','Panoramic sunset over the Pink City from the Aravallis.','adults, families','October to March','EASY',300,120,4.70,'https://images.unsplash.com/photo-1532664189809-02133fee698d?auto=format&fit=crop&w=900&q=80'),
(2,'Chokhi Dhani','jaipur-chokhi-dhani','A lively village-style evening of folk art and regional food.','families, adults','October to March','EASY',1200,240,4.40,'https://images.unsplash.com/photo-1603262110263-fb0112e7cc33?auto=format&fit=crop&w=900&q=80'),
(3,'Eravikulam National Park','munnar-eravikulam','Rolling grasslands and Nilgiri tahr habitat near Anamudi.','families, adults, seniors','September to May','EASY',300,180,4.70,'https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=900&q=80'),
(3,'Tea Museum','munnar-tea-museum','A compact story of Munnar tea, machinery and tasting.','families, adults, seniors','Year round','EASY',250,120,4.50,'https://images.unsplash.com/photo-1597318181409-cf64d0b5d8a2?auto=format&fit=crop&w=900&q=80'),
(3,'Mattupetty Dam','munnar-mattupetty-dam','Mountain reservoir with boating and valley scenery.','families, adults','September to May','EASY',400,150,4.40,'https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=900&q=80'),
(3,'Top Station','munnar-top-station','High viewpoint famous for cloud valleys and sunrise.','adults, families','October to May','EASY',300,180,4.60,'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=80'),
(3,'Kolukkumalai Sunrise Trek','munnar-kolukkumalai','A pre-dawn jeep-and-walk adventure to a tea estate.','adults, adventure','October to May','HARD',1600,360,4.80,'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=80'),
(3,'Spice Garden','munnar-spice-garden','Guided walk through cardamom, pepper and cinnamon gardens.','families, adults, seniors','Year round','EASY',350,120,4.30,'https://images.unsplash.com/photo-1596040033229-a9821ebd058d?auto=format&fit=crop&w=900&q=80'),
(4,'Baga Beach','goa-baga-beach','Lively beach with water sports, cafes and sunset walks.','families, adults','November to February','EASY',0,180,4.40,'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=900&q=80'),
(4,'Basilica of Bom Jesus','goa-basilica','Baroque Old Goa church and UNESCO World Heritage monument.','families, adults, seniors','November to February','EASY',100,120,4.60,'https://images.unsplash.com/photo-1582510003544-4d00b7f74220?auto=format&fit=crop&w=900&q=80'),
(4,'Dudhsagar Falls','goa-dudhsagar','A spectacular waterfall excursion through the Western Ghats.','adults, adventure','October to May','MODERATE',1800,420,4.70,'https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=900&q=80'),
(4,'Fontainhas Walk','goa-fontainhas','Colorful Latin quarter with heritage homes and bakeries.','families, adults, seniors','November to February','EASY',500,150,4.70,'https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=900&q=80'),
(4,'Palolem Beach','goa-palolem-beach','A calmer crescent beach with kayaking and cottage stays.','families, adults','November to February','EASY',0,180,4.60,'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=900&q=80'),
(4,'Spice Plantation Lunch','goa-spice-plantation','Traditional lunch and a walk through tropical spice groves.','families, adults, seniors','November to May','EASY',900,210,4.50,'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80'),
(5,'Dashashwamedh Ghat Aarti','varanasi-dashashwamedh-ghat','Evening Ganga Aarti viewed from the riverside.','families, adults, seniors','October to March','EASY',0,120,4.90,'https://images.unsplash.com/photo-1561361058-c24cecae35ca?auto=format&fit=crop&w=900&q=80'),
(5,'Sunrise Boat Ride','varanasi-sunrise-boat','Quiet sunrise boat journey past the historic ghats.','families, adults, seniors','October to March','EASY',600,120,4.80,'https://images.unsplash.com/photo-1561361058-c24cecae35ca?auto=format&fit=crop&w=900&q=80'),
(5,'Sarnath','varanasi-sarnath','Buddhist heritage site with museum and Dhamek Stupa.','families, adults, seniors','October to March','EASY',300,210,4.60,'https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=900&q=80'),
(5,'Kashi Vishwanath Corridor','varanasi-kashi-vishwanath','Important pilgrimage and newly restored temple corridor.','families, adults, seniors','October to March','EASY',0,150,4.80,'https://images.unsplash.com/photo-1561361058-c24cecae35ca?auto=format&fit=crop&w=900&q=80'),
(5,'Banarasi Silk Workshop','varanasi-silk-workshop','Meet artisans and see handloom silk weaving.','families, adults','Year round','EASY',450,150,4.50,'https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=900&q=80'),
(5,'Ramnagar Fort','varanasi-ramnagar-fort','Riverside fort museum across the Ganga.','families, adults','October to March','EASY',200,150,4.20,'https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=900&q=80');

INSERT INTO place_tags (place_id,tag) SELECT id,'nature' FROM places WHERE slug IN ('coorg-abbey-falls','coorg-mandalpatti','coorg-coffee-estate','coorg-nagarhole-safari','munnar-eravikulam','munnar-mattupetty-dam','munnar-top-station','munnar-spice-garden','goa-dudhsagar');
INSERT INTO place_tags (place_id,tag) SELECT id,'history' FROM places WHERE slug IN ('jaipur-amber-fort','jaipur-city-palace','jaipur-hawa-mahal','jaipur-jantar-mantar','goa-basilica','goa-fontainhas','varanasi-sarnath','varanasi-kashi-vishwanath','varanasi-ramnagar-fort');
INSERT INTO place_tags (place_id,tag) SELECT id,'food' FROM places WHERE slug IN ('coorg-coffee-estate','jaipur-chokhi-dhani','goa-spice-plantation','varanasi-silk-workshop');
INSERT INTO place_tags (place_id,tag) SELECT id,'beach' FROM places WHERE slug IN ('goa-baga-beach','goa-palolem-beach');
INSERT INTO place_tags (place_id,tag) SELECT id,'adventure' FROM places WHERE slug IN ('coorg-mandalpatti','coorg-nagarhole-safari','munnar-kolukkumalai','goa-dudhsagar');
INSERT INTO place_tags (place_id,tag) SELECT id,'temples' FROM places WHERE slug IN ('varanasi-dashashwamedh-ghat','varanasi-kashi-vishwanath');

INSERT INTO hotels (destination_id,name,description,address,star_rating,image_url) VALUES
(1,'Mistwood Retreat','Quiet plantation resort with breakfast and valley views.','Madikeri, Coorg',4.2,'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80'),
(2,'Pink City Courtyard','Central heritage stay within reach of the old city.','Bani Park, Jaipur',4.0,'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80'),
(3,'Tea Valley Residency','Comfortable hill stay with tea garden views.','Chithirapuram, Munnar',4.1,'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80');

INSERT INTO rooms (hotel_id,room_type,capacity,price_per_night,total_units,amenities) VALUES
(1,'Garden Room',2,3200,12,'["Breakfast","Wi-Fi","Garden view"]'),(1,'Family Cottage',4,5600,6,'["Breakfast","Wi-Fi","Living room"]'),(1,'Valley Suite',3,6800,4,'["Breakfast","Balcony","Valley view"]'),
(2,'Heritage Room',2,2800,15,'["Breakfast","Wi-Fi","AC"]'),(2,'Royal Family Room',4,4900,8,'["Breakfast","Wi-Fi","Two bathrooms"]'),(2,'Palace Suite',3,6200,3,'["Breakfast","Lounge","City view"]'),
(3,'Tea View Room',2,3000,14,'["Breakfast","Wi-Fi","Tea view"]'),(3,'Hill Family Room',4,5200,7,'["Breakfast","Balcony","Parking"]'),(3,'Premium Suite',3,6400,4,'["Breakfast","Bathtub","Valley view"]');

INSERT INTO vehicles (vehicle_type,model,seat_capacity,air_conditioned,price_per_day,price_per_km,driver_included,units_available,pickup_city) VALUES
('Hatchback','Maruti Swift',4,TRUE,2200,14,TRUE,8,'Madikeri'),('Sedan','Honda City',4,TRUE,3000,17,TRUE,7,'Jaipur'),('SUV','Toyota Innova Crysta',6,TRUE,4800,22,TRUE,6,'Munnar'),
('Tempo Traveller','Force Urbania',12,TRUE,7200,30,TRUE,4,'Goa'),('Mini Bus','Ashok Leyland 20-seater',20,TRUE,10500,38,TRUE,3,'Varanasi'),('Jeep','Mahindra Thar',4,TRUE,3600,20,TRUE,5,'Madikeri');
INSERT INTO destination_vehicles (destination_id,vehicle_id) SELECT d.id,v.id FROM destinations d CROSS JOIN vehicles v WHERE (d.slug='coorg' AND v.vehicle_type IN ('Hatchback','Jeep')) OR (d.slug='jaipur' AND v.vehicle_type IN ('Sedan','SUV')) OR (d.slug='munnar' AND v.vehicle_type IN ('SUV','Sedan')) OR (d.slug='goa' AND v.vehicle_type IN ('Sedan','Tempo Traveller')) OR (d.slug='varanasi' AND v.vehicle_type IN ('SUV','Mini Bus'));

INSERT INTO packages (destination_id,title,slug,description,duration_days,price_per_person,inclusions,hotel_id,image_url) VALUES
(1,'Coorg Mist & Coffee Escape','coorg-mist-coffee','A relaxed plantation holiday with waterfalls, viewpoints and local flavors.',3,8999,'["Breakfast","Private transfers","Sightseeing"]',1,'https://images.unsplash.com/photo-1593693397690-362cb9666fc2?auto=format&fit=crop&w=1400&q=80'),
(1,'Coorg Wild Trails','coorg-wild-trails','A more active Coorg circuit with forest safari and Mandalpatti.',4,12999,'["Breakfast","Jeep trail","Safari permit"]',1,'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1400&q=80'),
(2,'Royal Jaipur Weekend','royal-jaipur-weekend','A first-timer route through the Pink Citys headline monuments.',3,10999,'["Breakfast","Heritage walk","Airport transfer"]',2,'https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1400&q=80'),
(2,'Jaipur Culture & Cuisine','jaipur-culture-cuisine','Forts, bazaars, sunset views and a memorable folk dinner.',4,14999,'["Breakfast","Cultural dinner","Guide"]',2,'https://images.unsplash.com/photo-1603262110263-fb0112e7cc33?auto=format&fit=crop&w=1400&q=80'),
(3,'Munnar Tea Hills','munnar-tea-hills','Cool weather, tea gardens and gentle viewpoints for a restorative break.',3,9499,'["Breakfast","Local transfers","Tea tasting"]',3,'https://images.unsplash.com/photo-1597318181409-cf64d0b5d8a2?auto=format&fit=crop&w=1400&q=80'),
(3,'Munnar Peaks & Sunrise','munnar-peaks-sunrise','A scenic four-day hill escape with an optional sunrise adventure.',4,13499,'["Breakfast","Park tickets","Sunrise transfer"]',3,'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1400&q=80'),
(4,'Goa Sun & Heritage','goa-sun-heritage','Easy beaches, Old Goa and a flavor-filled coastal evening.',4,11999,'["Breakfast","Beach transfer","Heritage walk"]',NULL,'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=1400&q=80'),
(5,'Varanasi Soul of the Ganga','varanasi-soul-ganga','Ghats, sunrise on the river, Sarnath and artisan stories.',3,9999,'["Breakfast","Boat ride","Local guide"]',NULL,'https://images.unsplash.com/photo-1561361058-c24cecae35ca?auto=format&fit=crop&w=1400&q=80');

INSERT INTO package_places (package_id,place_id,day_number,order_no,type,add_on_price) SELECT p.id,pl.id,1,1,'MANDATORY',0 FROM packages p JOIN places pl ON pl.slug IN ('coorg-abbey-falls','jaipur-amber-fort','munnar-eravikulam','goa-baga-beach','varanasi-dashashwamedh-ghat') WHERE p.destination_id=pl.destination_id;
INSERT INTO package_places (package_id,place_id,day_number,order_no,type,add_on_price) SELECT p.id,pl.id,1,2,'OPTIONAL',pl.price FROM packages p JOIN places pl ON pl.slug IN ('coorg-raja-seat','jaipur-hawa-mahal','munnar-tea-museum','goa-fontainhas','varanasi-sunrise-boat') WHERE p.destination_id=pl.destination_id;
INSERT INTO package_places (package_id,place_id,day_number,order_no,type,add_on_price) SELECT p.id,pl.id,2,1,'MANDATORY',0 FROM packages p JOIN places pl ON pl.slug IN ('coorg-coffee-estate','jaipur-city-palace','munnar-mattupetty-dam','goa-basilica','varanasi-sarnath') WHERE p.destination_id=pl.destination_id;
INSERT INTO package_places (package_id,place_id,day_number,order_no,type,add_on_price) SELECT p.id,pl.id,2,2,'ADD_ON',pl.price FROM packages p JOIN places pl ON pl.slug IN ('coorg-dubare-elephant-camp','jaipur-chokhi-dhani','munnar-spice-garden','goa-spice-plantation','varanasi-silk-workshop') WHERE p.destination_id=pl.destination_id;
INSERT INTO package_places (package_id,place_id,day_number,order_no,type,add_on_price) SELECT p.id,pl.id,3,1,'OPTIONAL',pl.price FROM packages p JOIN places pl ON pl.slug IN ('coorg-mandalpatti','jaipur-nahargarh','munnar-top-station','goa-palolem-beach','varanasi-kashi-vishwanath') WHERE p.destination_id=pl.destination_id;

INSERT INTO package_dates (package_id,travel_date,seats_total) SELECT id, DATE_ADD(CURRENT_DATE, INTERVAL 20 DAY), 30 FROM packages;
INSERT INTO package_dates (package_id,travel_date,seats_total) SELECT id, DATE_ADD(CURRENT_DATE, INTERVAL 50 DAY), 30 FROM packages;
INSERT INTO package_dates (package_id,travel_date,seats_total) SELECT id, DATE_ADD(CURRENT_DATE, INTERVAL 80 DAY), 24 FROM packages;

INSERT INTO hotels (destination_id,name,description,address,star_rating,image_url) VALUES
(4,'Casa Palms Goa','A bright coastal stay close to beaches and heritage lanes.','Panjim, Goa',4.0,'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80'),
(5,'Ganga Courtyard','A calm riverside-inspired stay with guided cultural walks.','Assi, Varanasi',4.0,'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80');
INSERT INTO rooms (hotel_id,room_type,capacity,price_per_night,total_units,amenities) SELECT id,'Coastal Room',2,3400,12,'["Breakfast","Wi-Fi","AC"]' FROM hotels WHERE name='Casa Palms Goa';
INSERT INTO rooms (hotel_id,room_type,capacity,price_per_night,total_units,amenities) SELECT id,'Family Coastal Suite',4,5900,6,'["Breakfast","Wi-Fi","Balcony"]' FROM hotels WHERE name='Casa Palms Goa';
INSERT INTO rooms (hotel_id,room_type,capacity,price_per_night,total_units,amenities) SELECT id,'Ghat View Room',2,3100,12,'["Breakfast","Wi-Fi","River access"]' FROM hotels WHERE name='Ganga Courtyard';
INSERT INTO rooms (hotel_id,room_type,capacity,price_per_night,total_units,amenities) SELECT id,'Family Courtyard Suite',4,5400,6,'["Breakfast","Wi-Fi","Guide desk"]' FROM hotels WHERE name='Ganga Courtyard';
UPDATE packages p JOIN hotels h ON h.destination_id=p.destination_id SET p.hotel_id=h.id WHERE p.hotel_id IS NULL;

# Transport monitoring: 1-qadam (marshrutlash)

Maqsad: Spring Boot ichida GraphHopper ishlashini tekshirish va Toshkentda ikki nuqta orasidagi yo'lni xaritada ko'rish.

## Talablar
- Java 21, Maven 3.9+
- Kamida 2 GB bo'sh RAM (graf qurish paytida)

## Ishga tushirish

1. OSM ma'lumotini yuklab oling va `data/` papkaga qo'ying:

       https://download.geofabrik.de/asia/uzbekistan-latest.osm.pbf

   (Faqat Toshkentni qirqish uchun: `osmium extract -b 68.9,41.1,69.6,41.5 uzbekistan-latest.osm.pbf -o data/uzbekistan-latest.osm.pbf`)

2. Ishga tushiring:

       mvn spring-boot:run

   Birinchi marta graf qurilishi bir necha daqiqa oladi. Keyingi safar `graph-cache` dan tez yuklanadi.

3. Brauzerda oching: http://localhost:8080
   Xaritada ikki marta bosing: boshlanish va tugash nuqtasi. Yo'l yashil chiziq bilan chiziladi.

4. API'ni to'g'ridan-to'g'ri tekshirish:

       http://localhost:8080/api/route?fromLat=41.3111&fromLon=69.2797&toLat=41.3680&toLon=69.2900

## Keyingi qadamlar
2. PostGIS (docker-compose.yml tayyor): Vehicle, GpsPoint entity'lari, `POST /api/gps`
3. WebSocket bilan real vaqtda mashinani xaritada ko'rsatish
4. Tezlik segmentlari va tirbandlik

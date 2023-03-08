-- MySQL에 JSON 파일 Shop 테이블에 import 후 create_date, modify_date, favorite_cnt 초기화하기
UPDATE shop SET create_date = Now() WHERE create_date IS NULL;
UPDATE shop SET modify_date = Now() WHERE modify_date IS NULL;
UPDATE shop SET favorite_cnt = 0 WHERE favorite_cnt IS NULL;

/*
INSERT INTO SHOP(create_date, modify_date, road_address_name, place_name, brand, favorite_cnt)VALUES (NOW(), NOW(), '서울 성동구 서울숲2길 48', '인생네컷 서울숲노가리마트로드점', '인생네컷', 0);
INSERT INTO SHOP(create_date, modify_date, road_address_name, place_name, brand, favorite_cnt) VALUES(NOW(), NOW(), '서울 성동구 서울숲2길 17-2', '포토이즘박스 성수점', '포토이즘박스', 0);
INSERT INTO SHOP(create_date, modify_date, road_address_name, place_name, brand, favorite_cnt) VALUES(NOW(), NOW(), '서울 성동구 서울숲4길 13', '인생네컷 카페성수로드점', '인생네컷', 0);
INSERT INTO SHOP(create_date, modify_date, road_address_name, place_name, brand, favorite_cnt) VALUES(NOW(), NOW(), '서울 성동구 서울숲2길 45', '하루필름 서울숲점', '하루필름', 0);
INSERT INTO SHOP(create_date, modify_date, road_address_name, place_name, brand, favorite_cnt) VALUES(NOW(), NOW(), '서울 성동구 서울숲4길 20', '인생네컷 서울숲점', '인생네컷', 0);
INSERT INTO SHOP(create_date, modify_date, road_address_name, place_name, brand, favorite_cnt) VALUES(NOW(), NOW(), '서울 성동구 서울숲4길 23-1', '픽닷', '픽닷', 0);
INSERT INTO SHOP(create_date, modify_date, road_address_name, place_name, brand, favorite_cnt) VALUES(NOW(), NOW(), '충남 천안시 서북구 원두정2길 21', '인생네컷 충남천안두정먹거리공원점', '인생네컷', 0);
INSERT INTO SHOP(create_date, modify_date, road_address_name, place_name, brand, favorite_cnt) VALUES(NOW(), NOW(), '충남 천안시 동남구 먹거리10길 14', '하루필름 천안점', '하루필름', 0);
INSERT INTO SHOP(create_date, modify_date, road_address_name, place_name, brand, favorite_cnt) VALUES(NOW(), NOW(), '충남 천안시 서북구 원두정2길 21', '포토이즘박스 두정점', '포토이즘박스', 0);
INSERT INTO SHOP(create_date, modify_date, road_address_name, place_name, brand, favorite_cnt) VALUES(NOW(), NOW(), '충남 천안시 동남구 먹거리11길 28', '포토이즘컬러드 천안신부점', '포토이즘박스', 0);

-- ShopTitle
INSERT INTO shop_title(create_date, modify_date, name, conditions, content) VALUES (NOW(), NOW(), '핫 플레이스', '찜 수 5개 이상', '사람들이 주로 이용하는 포토부스에요.');
INSERT INTO shop_title(create_date, modify_date, name, conditions, content) VALUES (NOW(), NOW(), '청결 양호', '청결 점수 4점 이상', '매장이 깔끔해요.');
INSERT INTO shop_title(create_date, modify_date, name, conditions, content) VALUES (NOW(), NOW(), '보정 양호', '보정 점수 4점 이상', '사진이 잘 나와요.');
INSERT INTO shop_title(create_date, modify_date, name, conditions, content) VALUES (NOW(), NOW(), '소품 양호', '소품 점수 4점 이상', '다양하게 연출하기 좋아요.');

-- 칭호
INSERT INTO MEMBER_TITLE(create_date, modify_date, name, content) VALUES (NOW(), NOW(), '뉴비', '네컷지도 가입');
INSERT INTO MEMBER_TITLE(create_date, modify_date, name, content) VALUES (NOW(), NOW(), '리뷰 첫 걸음', '리뷰 1회 누적');
INSERT INTO MEMBER_TITLE(create_date, modify_date, name, content) VALUES (NOW(), NOW(), '리뷰 홀릭', '리뷰 5회 누적');
INSERT INTO MEMBER_TITLE(create_date, modify_date, name, content) VALUES (NOW(), NOW(), '찜 첫 걸음', '찜 1회 누적');
INSERT INTO MEMBER_TITLE(create_date, modify_date, name, content) VALUES (NOW(), NOW(), '찜 홀릭', '찜 5회 누적');
*/
DELETE FROM bookings;
DELETE FROM items;
DELETE FROM item_requests;
DELETE FROM comments;
DELETE FROM users;
DELETE FROM booking_statuses;

INSERT INTO booking_statuses (status) VALUES ('ALL');
INSERT INTO booking_statuses (status) VALUES ('APPROVED');
INSERT INTO booking_statuses (status) VALUES ('CURRENT');
INSERT INTO booking_statuses (status) VALUES ('PAST');
INSERT INTO booking_statuses (status) VALUES ('FUTURE');
INSERT INTO booking_statuses (status) VALUES ('WAITING');
INSERT INTO booking_statuses (status) VALUES ('REJECTED');
INSERT INTO booking_statuses (status) VALUES ('CANCELED');

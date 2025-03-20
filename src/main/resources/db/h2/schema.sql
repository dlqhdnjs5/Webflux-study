CREATE TABLE IF NOT EXISTS book (
    book_id bigint Not Null AUTO_INCREMENT,
    title_korean varchar(100) Not Null,
    title_english varchar(100) Not Null,
    description varchar(1000) Not Null,
    author varchar(100) Not Null,
    isbn varchar(100) Not Null,
    publish_date datetime Not Null,
    modified_at datetime Not Null,
    created_at datetime Not Null,
    primary key (book_id)
);
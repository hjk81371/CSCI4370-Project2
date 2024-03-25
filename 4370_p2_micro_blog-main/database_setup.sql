-- Create the database.
create database if not exists cs4370_mb_platform;

-- Use the created database.
use cs4370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null,
    primary key (userId),
    unique (username),
    constraint userName_min_length check (char_length(trim(userName)) >= 2),
    constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);

create table if not exists post (
    postId int auto_increment,
    userId int not null,
    postDate datetime not null,
    postText varchar(8000) not null,
    primary key (postId),
    foreign key (userId) references user(userId)
);

create table if not exists comment (
    commentId int auto_increment,
    postId int not null,
    userId int not null,
    commentDate varchar(255) not null,
    commentText varchar(8000),
    primary key (commentId),
    foreign key (postId) references post(postId),
    foreign key (userId) references user(userId)
);

create table if not exists heart (
    postId int not null,
    userId int not null,
    primary key (postId, userId),
    foreign key (postId) references post(postId),
    foreign key (userId) references user(userId)
);

create table if not exists bookmark (
    postId int not null,
    userId int not null,
    primary key (postId, userId),
    foreign key (postId) references post(postId),
    foreign key (userId) references user(userId)
);

create table if not exists hashtag (
    hashTag varchar(255),
    postId int not null,
    primary key (hashTag, postId),
    foreign key (postId) references post(postId)
);

create table if not exists follow (
    followerUserId int not null,
    followeeUserId int not null,
    primary key (followerUserId, followeeUserId),
    foreign key (followerUserId) references user(userId),
    foreign key (followeeUserId) references user(userId)
);

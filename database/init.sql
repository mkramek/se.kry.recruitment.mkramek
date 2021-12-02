use dev;

drop table if exists services, users;

create table `users` (
  `id` int(11) auto_increment primary key not null,
  `username` varchar(255) not null,
  `password` varchar(255) not null
);

create table `services` (
  `id` int(11) auto_increment primary key not null,
  `name` varchar(255) not null,
  `url` varchar(255) not null,
  `user` int(11) not null,
  `created_at` timestamp not null,
  `last_status` varchar(255) null,
  constraint fk_user foreign key (user) references users(id)
);

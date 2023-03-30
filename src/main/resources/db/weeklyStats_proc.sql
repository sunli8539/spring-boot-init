drop PROCEDURE if EXISTS  weeklyStats;
create PROCEDURE weeklyStats()
begin 
 DECLARE user_name varchar(50);
 DECLARE s int DEFAULT 0;
 DECLARE users CURSOR for select DISTINCT(create_by) from resource_occupy where start_time > CURRENT_DATE -7 ;
 DECLARE CONTINUE HANDLER  for  not found  set s=1;
 open users;
	 fetch users into user_name;
	 truncate table weekly_stats;
	 
	 while s<>1 DO
	    insert into weekly_stats select * from  (
	    select w3, name, start_time, @i:=@i +num as total from (
	    select create_by w3, u.name,  start_time,  resource_id , count(1) num
	    from  resource_occupy  o left join users u on o.create_by = u.user_name
    	where start_time  BETWEEN CURRENT_DATE -7  and CURRENT_DATE  and create_by = user_name
	    GROUP BY create_by,  start_time,  resource_id
	    ORDER BY create_by,  start_time 
	    ) a  ,  (select @i:=0) b ) tmp ;	 

	 fetch users into user_name;
	end while	;
  close users;
   select * from weekly_stats ORDER BY isnull(name)  ;
end;

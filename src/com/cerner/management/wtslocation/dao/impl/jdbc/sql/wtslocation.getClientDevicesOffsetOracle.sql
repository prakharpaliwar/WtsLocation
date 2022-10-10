with r as
(
	select
		row_number() over(order by clientname, logical_domain_id) as row_num,
		clientmnemonic,
		millenvironment,
		clientname,
		logical_domain_id
	from
		locations
	where
		lower(clientmnemonic) = ?
		and lower(millenvironment) = ?
)
select l.* from locations l
	inner join 
		r
	on
		lower(r.clientmnemonic) = lower(l.clientmnemonic)
		and lower(r.millenvironment) = lower(l.millenvironment)
		and r.clientname = l.clientname
		and r.logical_domain_id = l.logical_domain_id
	where 
		lower(l.clientmnemonic) = ?
		and lower(l.millenvironment) = ?
		and r.row_num >= ?
		and r.row_num <= ?
		
   

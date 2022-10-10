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
		clientmnemonic = ?
		and millenvironment = ?
)
select l.* from locations l
	inner join 
		r
	on
		r.clientmnemonic = l.clientmnemonic
		and r.millenvironment = l.millenvironment
		and r.clientname = l.clientname
		and r.logical_domain_id = l.logical_domain_id
	where 
		l.clientmnemonic = ?
		and l.millenvironment = ?
		and r.row_num >= ?
		and r.row_num <= ?
   


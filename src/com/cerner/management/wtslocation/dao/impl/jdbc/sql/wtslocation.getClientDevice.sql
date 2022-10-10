select * from locations 
	where clientname = ?
	and clientmnemonic = ? 
	and millenvironment = ?
	and logical_domain_id = ?
	order by clientname
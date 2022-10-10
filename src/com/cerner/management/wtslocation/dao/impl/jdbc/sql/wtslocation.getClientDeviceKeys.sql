select clientmnemonic, clientname, logical_domain_id
	from locations
	where lower(clientmnemonic) like ?
	and lower(millenvironment) = ? 
	order by clientname
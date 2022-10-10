select * from locations 
	where lower(clientmnemonic) like ? 
	and lower(millenvironment) = ? 
	order by clientname
   


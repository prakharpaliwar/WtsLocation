delete from locations
 where lower(clientname) = ? 
    and lower(clientmnemonic) = ? 
    and lower(millenvironment) = ?
    and logical_domain_id = ?
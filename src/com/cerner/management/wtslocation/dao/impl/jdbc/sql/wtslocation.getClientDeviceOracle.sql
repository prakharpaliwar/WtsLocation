select * from locations 
    where clientname = ?
    and lower(clientmnemonic) = ? 
    and lower(millenvironment) = ?
    and logical_domain_id = ?
    order by clientname
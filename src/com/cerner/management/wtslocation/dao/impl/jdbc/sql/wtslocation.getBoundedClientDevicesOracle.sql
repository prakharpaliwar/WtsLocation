select * from locations 
    where lower(clientmnemonic) like ? 
    and lower(millenvironment) = ? 
    {0}
    order by clientname
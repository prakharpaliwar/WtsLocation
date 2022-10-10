update locations
   set clientname = ?{0}
  where lower(clientname) = ? 
    and lower(clientmnemonic) = ? 
    and lower(millenvironment) = ?
    and logical_domain_id = ?
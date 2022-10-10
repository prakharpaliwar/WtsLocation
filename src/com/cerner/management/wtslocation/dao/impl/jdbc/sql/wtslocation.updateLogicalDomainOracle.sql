update locations
   set logical_domain_id = ?{0}
  where lower(clientname) = lower(?) 
    and lower(clientmnemonic) = lower(?) 
    and lower(millenvironment) = lower(?)
    and logical_domain_id = ?
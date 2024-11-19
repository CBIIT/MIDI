select original_instance_uid, filename
from `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_data` 
where original_instance_uid not in 
(select original_instance_uid from `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_ctp`)
group by original_instance_uid, filename
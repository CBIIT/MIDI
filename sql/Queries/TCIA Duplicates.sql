select original_instance_uid, filename
from `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_data` 
where original_instance_uid in
(SELECT original_instance_uid 
FROM `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_data` 
where tag='(0010,0020)'
group by original_instance_uid,
         tag
having count(*)>1)
group by original_instance_uid, filename
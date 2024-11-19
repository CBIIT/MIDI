select original_instance_uid, filename
from `nih-nci-cbiit-midi-dev2.comparisonscript.analysis_tcia_source` 
where original_instance_uid in
(SELECT original_instance_uid 
FROM `nih-nci-cbiit-midi-dev2.comparisonscript.analysis_tcia_source` 
where tag='(0010,0020)'
group by original_instance_uid,
         tag
having count(*)>1)
group by original_instance_uid, filename
order by original_instance_uid
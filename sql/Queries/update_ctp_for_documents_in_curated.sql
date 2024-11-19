update `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_ctp` run1 set run1.in_curated_doc = 'Y'
where run1.original_instance_uid in (select distinct run2.original_instance_uid
from `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_data` run2)
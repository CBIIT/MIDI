SELECT run2.original_instance_uid, run2.filename, run2.value
  FROM `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_data` run1
  full join `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_source` run2
  on run1.original_instance_uid=run2.original_instance_uid
 WHERE run1.original_instance_uid is null
 and run2.tagname like 'Modality'
 GROUP BY run2.original_instance_uid, run2.filename, run2.value

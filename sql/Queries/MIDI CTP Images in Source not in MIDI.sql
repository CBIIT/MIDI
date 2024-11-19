SELECT run2.original_instance_uid, run2.filename
  FROM `nih-nci-cbiit-midi-dev2.comparisonscript.midi-tcia_ctp` run1
  full join `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_ctp` run2
  on run1.original_instance_uid=run2.original_instance_uid
 WHERE run1.original_instance_uid is null
 GROUP BY run2.original_instance_uid, run2.filename

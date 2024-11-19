SELECT run1.tagname, count(*)
 FROM `nih-nci-cbiit-midi-dev2.comparisonscript.midi-tcia_source` run1
 join `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_source` run2
 on run1.original_instance_uid=run2.original_instance_uid
 and run1.tag=run2.tag
 and run1.sequence_number=run2.sequence_number
 and run1.value=run2.value
 and run1.tagname like '% UID'
 group by run1.tagname
 order by run1.tagname

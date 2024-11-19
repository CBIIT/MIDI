select midi.tagname, midi.data_element, count(*) number_of_values, run3.value source_value, midi.value midi_value
FROM
(SELECT run1.tagname, run1.tag data_element, run1.value, run1.original_instance_uid,run1.sequence_number
FROM `nih-nci-cbiit-midi-dev2.comparisonscript.midi-tcia_source` run1
full join
     `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_data` run2
 on run1.original_instance_uid=run2.original_instance_uid
and run1.tag=run2.tag
and run1.sequence_number=run2.sequence_number
where (run2.tag is null)
   and run1.tagname is not null
   and run1.value is not null
   and run1.in_curated_doc='Y'
   and run1.value <> ''
order by run1.tagname) midi
join `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_source` run3
on midi.original_instance_uid=run3.original_instance_uid
and midi.data_element=run3.tag
and midi.sequence_number=run3.sequence_number
and midi.tagname not like '% UID'
group by midi.tagname, midi.data_element, midi.value, run3.value
order by count(*) desc
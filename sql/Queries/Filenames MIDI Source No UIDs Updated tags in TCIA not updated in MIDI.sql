select tcia_updated_tags.tagname tagname, 
       tcia_updated_tags.tag data_element, 
       tcia_updated_tags.source_value_tcia source_value,  
       tcia_updated_tags.tcia_value, 
       midi.value midi_value,
       tcia_updated_tags.tcia_file,
       tcia_updated_tags.source_file,
       midi.filename
FROM
  (SELECT run2.tagname, 
          run2.tag, 
          run1.value tcia_value,
          run2.value source_value_tcia, 
          run2.original_instance_uid tcia_uid, 
          run2.sequence_number,
          run1.filename tcia_file,
          run2.filename source_file
   FROM `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_data` run1
   full join
        `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_source` run2
      on run1.original_instance_uid=run2.original_instance_uid
     and run1.tag=run2.tag
     and run1.sequence_number=run2.sequence_number
   where (run2.value is distinct from run1.value
          or run1.tag is null)
    and run1.original_instance_uid in (
         select original_instance_uid from `nih-nci-cbiit-midi-dev2.comparisonscript.midi-tcia_source`)) tcia_updated_tags
full join
  (SELECT run2.tagname, 
          run2.tag, 
          run1.value midi_value,
          run2.value source_value_midi, 
          run2.original_instance_uid midi_uid, 
          run2.sequence_number 
   FROM `nih-nci-cbiit-midi-dev2.comparisonscript.midi-tcia_source` run1
   full join
        `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_source` run2
      on run1.original_instance_uid=run2.original_instance_uid
     and run1.tag=run2.tag
     and run1.sequence_number=run2.sequence_number
   where (run2.value is distinct from run1.value
          or run1.tag is null)
      and run1.in_curated_doc='Y') midi_updated_tags
 on tcia_updated_tags.tag=midi_updated_tags.tag
and tcia_updated_tags.tcia_uid=midi_updated_tags.midi_uid
and tcia_updated_tags.sequence_number=midi_updated_tags.sequence_number
full join
    `nih-nci-cbiit-midi-dev2.comparisonscript.midi-tcia_source` midi
    on midi.tag=tcia_updated_tags.tag
    and midi.original_instance_uid=tcia_updated_tags.tcia_uid
    and midi.sequence_number=tcia_updated_tags.sequence_number
where midi_updated_tags.tag is null
  and midi.value is not distinct from tcia_updated_tags.source_value_tcia
  and tcia_updated_tags.tagname not like '% UID'

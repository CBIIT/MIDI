select tcia_updated_tags.tagname tagname, 
       tcia_updated_tags.tag data_element, 
       midi_updated_tags.source_value_midi source_value, 
       tcia_updated_tags.tcia_value,  
       midi_updated_tags.midi_value, 
       count(*) count_of_tags
FROM
  (SELECT run1.tagname, 
          run1.tag, 
          run1.value tcia_value, 
          run2.value source_value_tcia, 
          run1.original_instance_uid tcia_uid, 
          run1.sequence_number
   FROM `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_data` run1
    join
        `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_source` run2
     on run1.original_instance_uid=run2.original_instance_uid
    and run1.tag=run2.tag
    and run1.sequence_number=run2.sequence_number
  where (run2.value is distinct from run1.value)
    and run1.tag is not null) tcia_updated_tags
join
  (SELECT run1.tagname, 
          run1.tag, 
          run1.value midi_value,
          run2.value source_value_midi, 
          run1.original_instance_uid midi_uid, 
          run1.sequence_number
   FROM `nih-nci-cbiit-midi-dev2.comparisonscript.midi-tcia_source` run1
   join
        `nih-nci-cbiit-midi-dev2.comparisonscript.tcia-tcia_source` run2
      on run1.original_instance_uid=run2.original_instance_uid
     and run1.tag=run2.tag
     and run1.sequence_number=run2.sequence_number
   where (run2.value is distinct from run1.value)
      and run1.tag is not null) midi_updated_tags
 on tcia_updated_tags.tag=midi_updated_tags.tag
and tcia_updated_tags.tcia_uid=midi_updated_tags.midi_uid
and tcia_updated_tags.sequence_number=midi_updated_tags.sequence_number
where tcia_updated_tags.tcia_value is distinct from midi_updated_tags.midi_value
group by tcia_updated_tags.tagname, 
         tcia_updated_tags.tag, 
         tcia_updated_tags.tcia_value, 
         tcia_updated_tags.source_value_tcia,
         midi_updated_tags.tagname, 
         midi_updated_tags.tag, 
         midi_updated_tags.midi_value, 
         midi_updated_tags.source_value_midi
order by count(*) desc
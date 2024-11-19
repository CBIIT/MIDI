select COUNT(distinct run1.original_instance_uid) from `nih-nci-cbiit-midi-dev2.comparisonscript.group-1-2-SOURCE` run1  
 where run1.original_instance_uid not in        
           (select distinct run2.original_instance_uid
             from `nih-nci-cbiit-midi-dev2.comparisonscript.group-1-2-MIDI` run2) 
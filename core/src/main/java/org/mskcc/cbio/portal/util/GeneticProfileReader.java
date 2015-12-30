/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;

/**
 * Prepare a GeneticProfile for having its data loaded.
 * 
 * @author Ethan Cerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class GeneticProfileReader {

   /**
    * Load a GeneticProfile. Get a stableID from a description file. If the same
    * GeneticProfile already exists in the dbms use it, otherwise create a new
    * GeneticProfile dbms record, defining all parameters from the file.
    * 
    * @author Ethan Cerami
    * @author Arthur Goldberg goldberg@cbio.mskcc.org
    * 
    * @param file
    *           A handle to a description of the genetic profile, i.e., a
    *           'description' or 'meta' file.
    * @return an instantiated GeneticProfile record
    * @throws IOException
    *            if the description file cannot be read
    * @throws DaoException
    */
   public static GeneticProfile loadGeneticProfile(File file /*, int updateAction*/ ) throws IOException, DaoException {
      GeneticProfile geneticProfile = loadGeneticProfileFromMeta(file);
      GeneticProfile existingGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(geneticProfile
               .getStableId());

      if (existingGeneticProfile != null) {
         // the dbms already contains a GeneticProfile with the file's stable_id
         System.out.println("Warning: Possible Error: Existing Profile Found with Stable ID:  "
                  + existingGeneticProfile.getStableId());
      } else {
         // add new profile
         DaoGeneticProfile.addGeneticProfile(geneticProfile);
      }
      
      // Get ID
      GeneticProfile gp = DaoGeneticProfile.getGeneticProfileByStableId(geneticProfile.getStableId());
      geneticProfile.setGeneticProfileId(gp.getGeneticProfileId());
      return geneticProfile;
   }

   /**
    * Load a GeneticProfile from a description file.
    * 
    * @author Ethan Cerami
    * @author Arthur Goldberg goldberg@cbio.mskcc.org
    * 
    * @param file
    *           A handle to a description of the genetic profile, i.e., a
    *           'description' or 'meta' file.
    * @return an instantiated GeneticProfile
    * @throws IOException
    *            if the description file cannot be read
    * @throws DaoException
    */
   public static GeneticProfile loadGeneticProfileFromMeta(File file) throws IOException, DaoException {
      Properties properties = new Properties();
      properties.load(new FileInputStream(file));
      
      // when loading cancer studies and their profiles from separate files, 
      // use the cancer_study_identifier as a unique id for each study.
      // this was called the "cancer_type_id" previously.
      // eventually, it won't be needed when studies are loaded by a connected client that
      // knows its study_id in its state
      
      String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");
      if (cancerStudyIdentifier == null) {
         throw new IllegalArgumentException("cancer_study_identifier is not specified.");
      }
      CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
      if (cancerStudy == null) {
         throw new IllegalArgumentException("cancer study identified by cancer_study_identifier "
                  + cancerStudyIdentifier + " not found in dbms.");
      }
      
      String stableId = properties.getProperty("stable_id");
      if (stableId == null) {
         throw new IllegalArgumentException("stable_id is not specified.");
      }

      String profileName = properties.getProperty("profile_name");
      String profileDescription = properties.getProperty("profile_description");
      String geneticAlterationTypeString = properties.getProperty("genetic_alteration_type");
	  String datatype = properties.getProperty("datatype");
      if (profileName == null) {
         profileName = geneticAlterationTypeString;
      }
      
      if (profileDescription == null) {
         profileDescription = geneticAlterationTypeString;
      }
      
      if (geneticAlterationTypeString == null) {
         throw new IllegalArgumentException("genetic_alteration_type is not specified.");
      } else if (datatype == null) {
		  datatype = "";
	  }

      boolean showProfileInAnalysisTab = true;
      String showProfileInAnalysisTabStr = properties.getProperty("show_profile_in_analysis_tab");
      if (showProfileInAnalysisTabStr != null && showProfileInAnalysisTabStr.equalsIgnoreCase("FALSE")) {
         showProfileInAnalysisTab = false;
      }
      //check if the profile is mapped to any normal dataset
      String normalMappingSet = properties.getProperty("normal_mapping_set");
      int normalMappingID = -1;
      if(normalMappingSet != null){
    	  try {
    		  //check if the normal dataset is present, else throw IllegalArgumentException
			boolean isNormalDatasetPresent = DaoTumorVsNormal.isTechnologyPresent(normalMappingSet.toLowerCase());
			if(isNormalDatasetPresent){
				normalMappingID = DaoTumorVsNormal.getNormalMappingID(normalMappingSet.toLowerCase());
			}else{
				  throw new IllegalArgumentException("Normal Mapping dataste not found : "+normalMappingSet);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
      }

      profileDescription = profileDescription.replaceAll("\t", " ");
      GeneticAlterationType alterationType = GeneticAlterationType.getType(geneticAlterationTypeString);

      GeneticProfile geneticProfile = new GeneticProfile();
      geneticProfile.setCancerStudyId(cancerStudy.getInternalId());
      geneticProfile.setStableId(stableId);
      geneticProfile.setProfileName(profileName);
      geneticProfile.setProfileDescription(profileDescription);
      geneticProfile.setGeneticAlterationType(alterationType);
	  geneticProfile.setDatatype(datatype);
      geneticProfile.setShowProfileInAnalysisTab(showProfileInAnalysisTab);
      geneticProfile.setTargetLine(properties.getProperty("target_line"));
      geneticProfile.setNormalTissueMappingID(normalMappingID);
      return geneticProfile;
   }
}
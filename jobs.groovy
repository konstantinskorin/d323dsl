def command = "git ls-remote -h https://github.com/konstantinskorin/d323dsl.git"
def selectedBranches = command.execute().text.readLines().collect {it.split()[1].replaceAll('refs/heads/', '')}
  selectedBranches.removeAll {!(["master","kskorin"].contains(it)) }

job("MNTLAB-kskorin-main-build-job") {
  
    
    parameters {
      choiceParam('BRANCH_NAME',  selectedBranches,'')
      activeChoiceParam('BUILDS_TRIGGER') {
            description('Choose: ')
            choiceType('CHECKBOX')
            groovyScript {
                script('''list = []
				for(i in 1..4){
  				list.add("MNTLAB-kskorin-child${i}-build-job")
    			}
				return list''')
            	}
   		}
    }
  steps {
        downstreamParameterized {
                trigger('$BUILDS_TRIGGER') {
                        block{
                                buildStepFailure('FAILURE')
                                failure('FAILURE')
                                unstable('UNSTABLE')
                        }
                        parameters {
                                predefinedProp('BRANCH_NAME', '$BRANCH_NAME')
                                                }
                }
        }    
    }
}
for(i in 1..4){   
    //create downstream job
    job("MNTLAB-kskorin-child${i}-build-job") {
      scm {
        github('konstantinskorin/d323dsl', 'kskorin')
      }
      def allBranches = command.execute().text.readLines().collect {it.split()[1].replaceAll('refs/heads/', '')}
      allBranches.remove("kskorin")
      allBranches.add(0,"kskorin")
      parameters {
        choiceParam('BRANCH_NAME',  allBranches,'')
      }    
      steps {
      shell('''bash script.sh > output.txt
	       tar -czf ${BRANCH_NAME}_dsl_script.tar.gz script.sh jobs.groovy''')
      }
      publishers {
       			archiveArtifacts '${BRANCH_NAME}_dsl_script.tar.gz, output.txt'
     }
    }
}

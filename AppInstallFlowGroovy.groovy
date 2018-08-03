import groovy.json.JsonSlurper
import groovy.util.XmlSlurper
import jenkins.*
import jenkins.model.*
import hudson.model.*

def GetDeploymentTargertAndDeploy(Environment, ArtifactVersion)
{
getBuildXml()
   def DeploymentTargetFile = "DeploymentTarget_${Environment}.json"
   
   getDeploymentTargets(DeploymentTargetFile)
   
   def DeploymentTarget = readFile('deploymenttarget') 

   def ServerName = []

  ServerName=getServer(DeploymentTarget)


   for (i=0; i < ServerName.size(); i++){
      Deploy (ServerName[i], ArtifactVersion)
   }
}

@NonCPS
def getServer(DeploymentTarget)
{

  def jsonSlurper = new JsonSlurper()
  
  def deploymentTargetList = jsonSlurper.parseText (DeploymentTarget)   
  def ServerName = []
   for (i in   0..deploymentTargetList.size()-1) {
      ServerName << deploymentTargetList[i].Server
    
   }
   return ServerName
}

def getBuildXml(DeploymentTargetFile){

sh "curl  https://vcm.wal-mart.com/projects/SYSNIM/repos/deploymentautomation/browse/build.xml?raw > build.xml"

}


def getDeploymentTargets(DeploymentTargetFile){

sh "curl  https://vcm.wal-mart.com/projects/SYSNIM/repos/deploymentautomation/browse/${DeploymentTargetFile}?raw > deploymenttarget"

}

def Deploy(Server, ArtifactVersion)
{

sh "echo ServerName ${Server}"
sh "echo ArtifactVersion ${ArtifactVersion}"

ExecuteSSH (Server, "cd /u/gls/rmt; wget -O AppInstall.sh -q --retry-connrefused --waitretry=1 -t 0 --no-check-certificate -N https://vcm.wal-mart.com/projects/NIMAUT/repos/applicationdeploymentautomation/browse/AppInstall.sh?raw; chmod 777 /u/gls/rmt/AppInstall.sh; /u/gls/rmt/AppInstall.sh ${ArtifactVersion} /u/gls/NextGenAppInstall")

}

def ExecuteSSH (Server, cmd){

/*sh "sshpass -p 'l4virus' ssh -t -t -o UserKnownHostsFile=/dev/null -o \"StrictHostKeyChecking no\" -o \"PreferredAuthentications password\" -o \"PubKeyAuthentication no\" -o \"PasswordAuthentication yes\" gls@${Server}  '${cmd}'"*/

withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'deploymentCredentials', passwordVariable: 'password', usernameVariable: 'username']]) {
def username='$username'
def password='$password'
sh "ant -DServer=${Server} -Dusername=${username}  -Dpassword=${password} -Dcmd='${cmd}'"
}

}

return this;
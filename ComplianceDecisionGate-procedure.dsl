project 'Default'
procedure 'Application Decision Gate', {
  description = '### Compliance gate procedure - this procedure uses a hidden API not intended for production use ###'
  projectName = 'Default'
  timeLimit = '0'

  formalParameter 'externalApplicationId', {
    description = 'Unique identifier for the application used to identity the application in CloudBees Compliance.'
    label = 'External application ID'
    orderIndex = '1'
    required = '1'
    type = 'entry'
  }

  formalParameter 'releaseManifest', {
    description = '''List of components (artifacts or container images) in JSON format, that are being deployed for the application identified by the given external application ID.
E.g.,
<code>[
    {
      // Name of component that uniquely identifies
      // the artifact in CloudBees Compliance.
      // Must be specified.
      "name": "com.acme:shoppingCart",

      // Component version being deployed.
      // Must be specified.
      "version": "1.3.0",

      // Optional field used to identity where the
      // artifact is stored, e.g., maven.
      // If specified, will be used by
      // CloudBees Compliance to limit the scope
      // when searching for the artifact asset.
      // If not specified, the artifact will be
      // searched across all artifact asset types.
      "type": "maven"
    },
    {
      "name": "busybox:glibc",
      "version": "2.0"
    }

]</code>'''
    label = 'Application manifest'
    orderIndex = '2'
    required = '1'
    type = 'textarea'
  }

  formalParameter 'maxWaitTimeForGateResult', defaultValue: '30', {
    description = 'Maximum time to wait in minutes, for the gate result from CloudBees Compliance. The gate will error out if the result is still in process once the maximum wait time is reached.'
    label = 'Maximum wait time (in minutes) '
    orderIndex = '3'
    required = '1'
    type = 'integer'
  }


  step 'Application Specific Compliance Check ', {
    command = '''use strict;
use ElectricCommander;
use Switch;
use XML::XPath;
use Scalar::Util qw(looks_like_number);

# Turn off output buffering
$| = 1;

my $ec = new ElectricCommander({abortOnError => 0});
my $xpath = $ec->getActualParameter("releaseManifest",
        {"jobId" => "$[/myJob/jobId]"});
my $releaseManifest = $xpath->findvalue("//value")->value;

# default max timeout to 30 minutes
my $maxSeconds = 1800;
my $waitTimeInMins = "$[maxWaitTimeForGateResult]";
if (looks_like_number($waitTimeInMins) && $waitTimeInMins > 0) {
    $maxSeconds = $waitTimeInMins * 60;
}

my $startTime = time();
while (time() - $startTime < $maxSeconds) {

    print "Checking Compliance gate decision...\\n";
    #<externalApplicationId> <flowRuntimeStateId> <releaseManifest>
    $xpath = $ec->getComplianceGateDecision(
            "$[externalApplicationId]",
            "$[/myJob/flowRuntimeStateId]",
            $releaseManifest
            );
    my $errors = $ec->checkAllErrors($xpath);

    if ($errors) {
      $ec->setProperty("summary", $errors);
      print "$errors\\n";
      exit 1;
    }

    my $result = $xpath->findvalue("//result")->value;
    my $summary = $xpath->findvalue("//summary")->value;
    my $linkBackUrl = $xpath->findvalue("//linkBackUrl")->value;
    print "Result: $result\\n";
    print "Summary: $summary\\n";
    if ($linkBackUrl) {
      print "Compliance URL: $linkBackUrl\\n";
    }


    if ($summary) {
     $ec->setProperty("summary", "Gate decision: $summary");
     $ec->setProperty("/myPipelineStageRuntime/ec_summary/Exit gate decision", $summary);

     if ($linkBackUrl) {
       $ec->setProperty("/myJob/report-urls/Compliance gate report",  $linkBackUrl);
       eval {
          my $link = qq|<html><span class=\\"jobStep_statusText\\"><a target=\\"_blank\\" href=\\"$linkBackUrl\\">$summary</a></span><br /></html>|;

         $ec->setProperty("/myPipelineStageRuntime/ec_summary/Exit gate decision", $link);

       };
      }
    }

    switch ($result) {
       case("success") {
        exit 0;
       }
       case("failed") {
        exit 1;
       }
       case("error") {
        exit 1;
       }
       case("inprocess") {
        my $nowString = localtime;
        print "$nowString - Waiting for result...\\n";
        # sleep for 1 minute - expressed in code in seconds.
        sleep(60);
       }
       default {
        print "Unexpected result: $result\\n";
        exit 1;
       }
    }
}
# fell through waiting for result and timeout reached
my $timeoutMsg = "Timed out after $maxSeconds seconds waiting for Compliance gate decision.";
$ec->setProperty("summary", $timeoutMsg);
print "$timeoutMsg\\n";
exit 1;

'''
    shell = 'ec-perl'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
  }
}

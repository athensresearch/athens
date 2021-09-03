# Deployment Athens Self-Hosted on AWS

The Athens Self-Hosted server can be deployed on AWS using a [CloudFormation](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/Welcome.html) template we provide.

## TODOs

- consider refactoring to https://github.com/nathanpeck/ecs-cloudformation#publically-networked-service-with-public-load-balancer
- should using a nginx container as well to filter traffic to athens by http and so that we don't have to set the port on Athens.
- https cert support & instructions
- replace filipesilva/athens-server:rtc.alpha.2 image with official one

## Pre-requisites

You will need to [create and activate an AWS account](https://aws.amazon.com/premiumsupport/knowledge-center/create-and-activate-aws-account/).
Each new account starts with a [free tier](https://aws.amazon.com/premiumsupport/knowledge-center/what-is-free-tier/) that you can use to run Athens.
You can see how much costs you've incurred in the [Billing page](https://console.aws.amazon.com/billing/).

Next install the [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html) for your operating system, and use the [configure command](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html#cli-configure-quickstart-config) to finish setup.
The region you choose here is the region that Athens will be installed in.

We'll use `athens-demo` as a name for your Athens deployment, but you can use any other name.


## Create

Run the following command to automatically set up an Athens stack.

```sh
aws cloudformation create-stack \
    --capabilities CAPABILITY_NAMED_IAM \
    --template-url https://raw.githubusercontent.com/athensresearch/athens/main/data/self-hosted.cloudformation.yml \
    --stack-name athens-demo
```
```sh
aws cloudformation wait stack-create-complete --stack-name athens-demo
```
```sh
aws cloudformation describe-stacks \
    --stack athens-demo \
    --query "Stacks[].Outputs[?OutputKey=='NLBFullyQualifiedName'].OutputValue" \
    --output text
```

Use the domain name returned by the last command followed by `:3010` to connect to Athens server on the Athens client DB Picker.


## Update

Run the command below to restart the server and update to the latest Athens server:

```sh
aws ecs update-service --force-new-deployment --service athens-svc --cluster athens-cluster
```
```sh
aws ecs wait services-stable --service athens-svc --cluster athens-cluster
```


## Delete

You can remove Athens from your AWS account by running these commands:

```sh
aws cloudformation delete-stack --stack-name athens-demo
```
```sh
aws cloudformation wait stack-delete-complete --stack-name athens-demo
```


## Snapshots

Athens will take a snapshot every 12h and when you delete the stack.
Up to 90 snapshots of the ones taken every 12h will be retained for each stack, but snapshots taken on stack deletion will not be automatically removed.
You can use these snapshots recreate the Athens server with the data it had at the time of the snapshot.

To list all the snapshots you have, what stack they belong to, and when they were taken, run:
```sh
aws ec2 describe-snapshots \
    --filters Name=tag:Athens,Values=DataVolume \
    --query "Snapshots[*].{ID:SnapshotId,Time:StartTime,Stack:Tags[?Key=='AthensStack']|[0].Value} | sort_by([], &Time)" \
    --output table
```

You can then use the ID to when creating a new Athens stack via the `--SnapshotID` parameter.

For instance, for the ID `snap-05fae4803cf5ea081`, we can create `athens-demo-two` via:
```sh
aws cloudformation create-stack \
    --capabilities CAPABILITY_NAMED_IAM \
    --template-url https://raw.githubusercontent.com/athensresearch/athens/main/data/self-hosted.cloudformation.yml \
    --stack-name athens-demo-two \
    --parameters ParameterKey=SnapshotID,ParameterValue=snap-05fae4803cf5ea081
```

You can make a new Athens stack side-by-side with your existing ones.
This is useful to check past states or recover data deleted accidentally.


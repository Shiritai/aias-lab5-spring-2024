# State diagram of RobustCalculator

## State diagram of `(`

```mermaid
graph LR
    doHalt[["doHalt"]] --> doStartLevelPair --> doHalt
```

## State diagram of `*`

```mermaid
graph LR
    doHalt[["doHalt"]] --> doCheckAndEndNum --> doCheckAndEndLevelPair --> doFlushPairedParenthesisT --> doPopAndPushT
    doFlushPairedParenthesisT --> doCheckAndEndLevelPair
    doPopAndPushT --> doFlushPairedParenthesisT

    doCheckAndEndLevelPair --> doPushOperator --> doPopAndPushPrior
    doPopAndPushPrior --> doPushOperator
    doPushOperator --> doHalt
    doCheckAndEndLevelPair --> doCheckAndEndLevelPair
```

## State diagram of `+-`

```mermaid
graph LR
    doHalt[["doHalt"]] --> doCheckAndEndNum --> doCheckAndEndLevelPair <--> doFlushPairedParenthesisT <--> doPopAndPushT

    doCheckAndEndLevelPair --> doPushOperator <--> doPopAndPushPrior
    doPushOperator --> doHalt

    doCheckAndEndLevelPair --> doStartLevelPair --> doPushZero --> doPushEndSig --> doHalt
    doCheckAndEndLevelPair --> doCheckAndEndLevelPair
```

## State diagram of `)`

```mermaid
graph LR
    doHalt[["doHalt"]] --> doCheckAndEndNum --> doCheckAndEndLevelPair <--> doFlushPairedParenthesisT <--> doPopAndPushT

    doCheckAndEndLevelPair --> doFlushPairedParenthesisF <--> doPopAndPushF
    doFlushPairedParenthesisF --> doCheckAndEndLevelPair2 <-->
    doFlushPairedParenthesisT2 <--> doPopAndPushT2

    doCheckAndEndLevelPair2 --> doHalt
    doCheckAndEndLevelPair --> doCheckAndEndLevelPair
```

## State diagram of `=`

```mermaid
graph LR
    doHalt[["doHalt"]] --> doCheckAndEndNum --> doCheckAndEndLevelPair --> doFlushPairedParenthesisT --> doPopAndPushT

    doFlushPairedParenthesisT --> doCheckAndEndLevelPair
    doPopAndPushT --> doFlushPairedParenthesisT
    doCheckAndEndLevelPair --> doPopAndPush
    doPopAndPush --> doCheckAndEndLevelPair

    doCheckAndEndLevelPair --> doHalt

    doCheckAndEndLevelPair --> doCheckAndEndLevelPair
```

## State diagram of `0123456789`

```mermaid
graph LR
    doHalt[["doHalt"]] --> doPush --> doHalt
```
# State diagram of RobustCalculator

## State diagram of `(`

```mermaid
graph LR
    doHalt[["doHalt"]] --> doCheckAndEndNum --> doCheckAndEndLevelPair <--> doFlushPairedParenthesisT <--> doPopAndPushT

    doCheckAndEndLevelPair --> doStartLevelPair --> doHalt
    doCheckAndEndLevelPair --> doCheckAndEndLevelPair
```

## State diagram of `*`

```mermaid
graph LR
    doHalt[["doHalt"]] --> doCheckAndEndNum --> doCheckAndEndLevelPair <--> doFlushPairedParenthesisT <--> doPopAndPushT

    doCheckAndEndLevelPair --> doPushOperator <--> doPopAndPushPrior
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
    doHalt[["doHalt"]] --> doCheckAndEndNum --> doCheckAndEndLevelPair <--> doFlushPairedParenthesisT <--> doPopAndPushT

    doCheckAndEndLevelPair <--> doPopAndPush

    doCheckAndEndLevelPair --> doHalt

    doCheckAndEndLevelPair --> doCheckAndEndLevelPair
```

## State diagram of `0123456789`

```mermaid
graph LR
    doHalt[["doHalt"]] <--> doPush
```
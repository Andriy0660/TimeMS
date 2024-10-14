import {CircularProgress} from "@mui/material";
import Button from "@mui/material/Button";
import useAppContext from "../context/useAppContext.js";
import useSyncMutations from "../hooks/useSyncMutations.js";
import useAsyncCall from "../hooks/useAsyncCall.js";

export default function SyncWorklogsButton({children, className}) {
  const {isSyncingRunning, progressInfo: {progress}} = useAppContext();
  const {onSync} = useSyncMutations();

  const {execute: handleSyncWorklogs, isExecuting: isSyncingLaunched} = useAsyncCall({
    fn: onSync,
  })

  return (
    <Button onClick={handleSyncWorklogs} className={`bg-white ${className}`} sx={{color: "primary.main"}} disabled={isSyncingLaunched || isSyncingRunning}
            variant="contained">
      <ButtonContent
        isSyncingLaunched={isSyncingLaunched}
        isSyncingRunning={isSyncingRunning}
        progress={progress}
        className="ml-1"
      >
        {children}
      </ButtonContent>
    </Button>
  )
}

function ButtonContent({
  children,
  isSyncingLaunched,
  isSyncingRunning,
  progress,
  className
}) {
  if (!isSyncingLaunched && !isSyncingRunning) {
    return children;
  }

  if (isSyncingLaunched && !isSyncingRunning) {
    return <CircularProgress size={25} />
  }

  return (
    <>
      {`${Math.round(progress)}%`}
      <CircularProgress className={className} variant="determinate" size={25} value={progress} />
    </>
  );
}
import {useEffect, useRef, useState} from 'react';
import {CircularProgress} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import SyncProgressInfo from "../components/SyncProgressInfo.jsx";
import SyncAllProgressInfo from "../components/SyncAllProgressInfo.jsx";

export default function SyncPage() {
  const {
    isSyncingLaunched,
    isSyncingRunning,
    progressInfo: {
      worklogInfos,
    }
  } = useAppContext();

  const [displayedWorklogInfos, setDisplayedWorklogInfos] = useState([]);
  const logsContainerRef = useRef(null);
  const [isUserAtBottom, setIsUserAtBottom] = useState(true);

  useEffect(() => {
    if (!worklogInfos || !isSyncingRunning) return;
    setDisplayedWorklogInfos(prevLogs => [...prevLogs, ...worklogInfos]);
  }, [worklogInfos, isSyncingRunning]);

  const handleScroll = () => {
    if (!logsContainerRef.current) return;
    const {scrollTop, scrollHeight, clientHeight} = logsContainerRef.current;
    setIsUserAtBottom(scrollTop + clientHeight >= scrollHeight);
  };

  useEffect(() => {
    if (isUserAtBottom && logsContainerRef.current) {
      logsContainerRef.current.scrollTop = logsContainerRef.current.scrollHeight;
    }
  }, [displayedWorklogInfos, isUserAtBottom]);

  if (isSyncingLaunched && !isSyncingRunning) {
    if (displayedWorklogInfos.length > 0) {
      setDisplayedWorklogInfos([])
    }
    return (
      <div className="absolute inset-1/2">
        <CircularProgress />
      </div>
    );
  }

  return (
    <div className="w-3/5 mx-auto">
      <SyncAllProgressInfo />
      {displayedWorklogInfos.length > 0 && (
        <div
          className="mt-8 p-4 rounded-xl overflow-y-scroll h-[500px] bg-gray-500"
          ref={logsContainerRef}
          onScroll={handleScroll}
        >
          {displayedWorklogInfos.map((worklogInfo, index) => (
            <SyncProgressInfo
              className="mt-2"
              key={`${worklogInfo.ticket}-${index}`}
            >
              {`${worklogInfo.date} | ${worklogInfo.ticket} | ${worklogInfo.comment}`}
            </SyncProgressInfo>
          ))}
        </div>
      )}
    </div>
  );
}

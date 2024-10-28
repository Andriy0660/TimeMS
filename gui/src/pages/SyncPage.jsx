import {useEffect, useRef, useState} from 'react';
import SyncProgressInfo from "../components/sync/SyncProgressInfo.jsx";
import SyncAllProgressInfo from "../components/sync/SyncAllProgressInfo.jsx";
import LoadingPage from "../components/general/LoadingPage.jsx";
import useSync from "../hooks/useSync.js";

export default function SyncPage() {
  const {
    isSyncingLaunched,
    isSyncingRunning,
    progressInfo: {
      worklogInfos,
    }
  } = useSync();

  const [displayedWorklogInfos, setDisplayedWorklogInfos] = useState([]);
  const logsContainerRef = useRef(null);
  const [isUserAtBottom, setIsUserAtBottom] = useState(true);

  useEffect(() => {
    if (!worklogInfos || !isSyncingRunning) return;

    setDisplayedWorklogInfos(prevLogs => {
      const newLogs = [...prevLogs, ...worklogInfos];
      return newLogs.slice(-100);
    });
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
    return <LoadingPage />
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

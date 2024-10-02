import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import worklogApi from "../api/worklogApi.js";
import timeLogApi from "../api/timeLogApi.js";
import useAppContext from "../context/useAppContext.js";

export default function useWorklogSync() {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {mutateAsync: syncWorklogs, isPending: isSyncingLaunched} = useMutation({
    mutationFn: () => worklogApi.syncWorklogs(),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "You have successfully synchronized worklogs",
        type: "success"
      });
    },
    onError: (error) => {
      queryClient.setQueryData([worklogApi.key, "progress"], {progress: 0});
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("synchronizing worklogs failed:", error);
    }
  });

  const {
    data: progressInfo,
  } = useQuery({
    queryKey: [worklogApi.key, "progress"],
    queryFn: () => worklogApi.getProgress(),
    initialData: () => 0,
    refetchInterval: (data) => isSyncingLaunched || data.state.data.progress > 0 ? 300 : false,
    refetchOnWindowFocus: false,
    retryDelay: 300
  });

  const isSyncingRunning = isSyncingLaunched || progressInfo.progress > 0;

  return {
    syncWorklogs,
    progressInfo,
    isSyncingRunning,
    isSyncingLaunched
  }
}
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import worklogApi from "../api/worklogApi.js";
import useAppContext from "../context/useAppContext.js";
import syncJiraApi from "../api/syncJiraApi.js";
import {isJiraSyncingEnabled} from "../config/config.js";

export default function useJiraSync() {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {mutateAsync: syncWorklogs, isPending: isSyncingLaunched} = useMutation({
    mutationFn: () => syncJiraApi.syncAllWorklogs(),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogApi.key);
      queryClient.invalidateQueries(worklogApi.key);
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
    queryFn: () => syncJiraApi.getProgress(),
    initialData: () => 0,
    refetchInterval: (data) => isSyncingLaunched || data.state.data.inProgress ? 300 : false,
    enabled: isJiraSyncingEnabled,
    refetchOnWindowFocus: false,
    retryDelay: 300
  });

  const {mutateAsync: syncIntoJira} = useMutation({
    mutationFn: (body) => syncJiraApi.syncIntoJira(body),
    onSuccess: async () => {
      queryClient.invalidateQueries(worklogApi.key);
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Successfully synchronized into jira",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("synchronizing into jira failed:", error);
    }
  });

  const {mutateAsync: syncFromJira} = useMutation({
    mutationFn: (body) => syncJiraApi.syncFromJira(body),
    onSuccess: async () => {
      queryClient.invalidateQueries(worklogApi.key);
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Successfully synchronized from jira",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("synchronizing from jira failed:", error);
    }
  });


  const {mutateAsync: syncWorklogsForIssue} = useMutation({
    mutationFn: (issueKey) => syncJiraApi.syncWorklogsForIssue(issueKey),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: `You have successfully synchronized worklogs for issue ${variables}`,
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("synchronizing worklogs for issue failed:", error);
    }
  });
  return {
    progressInfo,
    isSyncingLaunched,
    isSyncingRunning: progressInfo.inProgress && progressInfo.progress > 0,
    onSyncIntoJira: syncIntoJira,
    onSyncFromJira: syncFromJira,
    onSync: syncWorklogs,
    onSyncForIssue: syncWorklogsForIssue
  }
}
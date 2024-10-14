import {useMutation, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import worklogApi from "../api/worklogApi.js";
import useAppContext from "../context/useAppContext.js";
import syncApi from "../api/syncApi.js";

export default function useSyncMutations() {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {mutateAsync: syncIntoJira} = useMutation({
    mutationFn: (body) => syncApi.syncIntoJira(body),
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
    mutationFn: (body) => syncApi.syncFromJira(body),
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

  const {mutateAsync: syncWorklogs, isPending: isSyncingLaunched} = useMutation({
    mutationFn: () => syncApi.syncAllWorklogs(),
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

  const {mutateAsync: syncWorklogsForIssue} = useMutation({
    mutationFn: (issueKey) => syncApi.syncWorklogsForIssue(issueKey),
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
    onSyncIntoJira: syncIntoJira,
    onSyncFromJira: syncFromJira,
    onSync: syncWorklogs,
    onSyncForIssue: syncWorklogsForIssue
  }
}
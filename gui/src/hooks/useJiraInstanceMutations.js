import {useMutation, useQueryClient} from "@tanstack/react-query";
import useAppContext from "../context/useAppContext.js";
import configApi from "../api/configApi.js";

export default function useJiraInstanceMutations() {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {mutateAsync: saveJiraInstance} = useMutation({
    mutationFn: (body) => configApi.saveJiraInstance(body),
    onSuccess: async () => {
      queryClient.invalidateQueries(configApi.key);
      addAlert({
        text: "Jira instance is successfully saved",
        type: "success"
      });
    },
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Saving jira instance failed:", error);
    }
  });

  const {mutateAsync: deleteJiraInstance} = useMutation({
    mutationFn: (id) => configApi.deleteJiraInstance(id),
    onSuccess: () => {
      queryClient.invalidateQueries(configApi.key);
      addAlert({
        text: "You have successfully deleted jira instance",
        type: "success"
      });
    },
    onError: (error) => {
      queryClient.invalidateQueries(configApi.key);
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Deleting jira instance failed:", error);
    }
  });

  return {
    onSave: saveJiraInstance,
    onDelete: deleteJiraInstance
  }
}
import {useMutation, useQueryClient} from "@tanstack/react-query";
import useAppContext from "../context/useAppContext.js";
import configApi from "../api/configApi.js";

export default function useConfigMutations() {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {mutateAsync: updateTimeConfig} = useMutation({
    mutationFn: (body) => configApi.updateTimeConfig(body),
    onSuccess: async () => {
      queryClient.invalidateQueries(configApi.key);
      addAlert({
        text: "Time configuration is successfully updated",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Updating time configuration failed:", error);
    }
  });

  const {mutateAsync: updateJiraConfig} = useMutation({
    mutationFn: (body) => configApi.updateJiraConfig(body),
    onSuccess: async () => {
      queryClient.invalidateQueries(configApi.key);
      addAlert({
        text: "Jira configuration is successfully updated",
        type: "success"
      });
    },
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Updating Jira configuration failed:", error);
    }
  });

  const {mutateAsync: updateExternalServiceConfig} = useMutation({
    mutationFn: (body) => configApi.updateExternalServiceConfig(body),
    onSuccess: async () => {
      queryClient.invalidateQueries(configApi.key);
      addAlert({
        text: "External Service configuration is successfully updated",
        type: "success"
      });
    },
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Updating External Service configuration failed:", error);
    }
  });

  return {
    onTimeConfigUpdate: updateTimeConfig,
    onJiraConfigUpdate: updateJiraConfig,
    onExternalServiceConfigUpdate: updateExternalServiceConfig,
  }
}
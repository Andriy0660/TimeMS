import {useMutation, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import useAppContext from "../context/useAppContext.js";
import syncExternalServiceApi from "../api/syncExternalService.js";
import externalTimeLogApi from "../api/externalTimeLogApi.js";

export default function useExternalServiceSync() {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {mutateAsync: syncIntoExternalService} = useMutation({
    mutationFn: (body) => syncExternalServiceApi.syncIntoExternalService(body),
    onSuccess: async () => {
      queryClient.invalidateQueries(externalTimeLogApi.key);
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Successfully synchronized into external service",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("synchronizing into external service failed:", error);
    }
  });
  return {
    onSyncIntoExternalService: syncIntoExternalService,
  }
}
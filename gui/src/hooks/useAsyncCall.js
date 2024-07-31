import {useState} from "react";

export default function useAsyncCall({fn, onSuccess, onError, onFinally}) {
  const [isExecuting, setIsExecuting] = useState(false);
  const [error, setError] = useState(null);

  return {
    execute: async (body) => {
      setIsExecuting(true);
      setError(null);
      try {
        const result = await fn(body);
        if (onSuccess) onSuccess(result);
      } catch (error) {
        setError(error);
        if (onError) onError(error);
      } finally {
        setIsExecuting(false);
        if (onFinally) onFinally();
      }
    },
    isExecuting,
    error,
    isError: !!error
  }
}
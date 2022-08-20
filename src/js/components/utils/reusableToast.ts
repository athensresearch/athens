/**
 * Creates or updates a toast message with the given options.
 * @param toast The toast function
 * @param ref The ref to store the toast
 * @param props 
 */
export const reusableToast = (toast, ref, props) => {
  if (ref.current) {
    toast.update(ref.current, {
      ...props,
      onCloseComplete: () => {
        props.onCloseComplete && props.onCloseComplete();
        ref.current = null;
      }
    });
  } else {
    ref.current = toast({
      ...props,
      onCloseComplete: () => {
        props.onCloseComplete && props.onCloseComplete();
        ref.current = null;
      }
    });
  }
}

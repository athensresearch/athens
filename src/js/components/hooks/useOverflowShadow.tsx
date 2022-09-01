import * as React from "react";

const normalizeValue = (value) => Math.max(0, value);

export const useOverflowBox = (ref) => {
  const [overflowBox, setOverflowBox] = React.useState({
    top: 0,
    right: 0,
    bottom: 0,
    left: 0
  });

  const onScroll = React.useCallback(() => {
    if (ref?.current) {
      const box = ref.current;
      setOverflowBox({
        top: normalizeValue(Math.floor(box.scrollTop)),
        right: normalizeValue(
          Math.floor(box.scrollWidth - box.clientWidth - box.scrollLeft)
        ),
        bottom: normalizeValue(
          Math.floor(box.scrollHeight - box.clientHeight - box.scrollTop)
        ),
        left: normalizeValue(Math.floor(box.scrollLeft))
      });
    }
  }, [ref, setOverflowBox]);

  React.useEffect(() => {
    onScroll();
  }, []);

  const hasOverflow =
    overflowBox.top ||
    overflowBox.bottom ||
    overflowBox.left ||
    overflowBox.right;

  return {
    onScroll,
    hasOverflow,
    overflowBox
  };
};

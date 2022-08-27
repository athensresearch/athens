import React from 'react';
import { LayoutContext } from './useLayoutState';
import { Box, BoxProps } from '@chakra-ui/react';

const MIN_VW = 20;
const MAX_VW = 80;


const clamp = (value: number, min: number, max: number) => Math.max(Math.min(value, max), min);
const getVW = (e, window) => {
  const innerWidth = window.innerWidth;
  const clientX = e.clientX;
  const calcVW = (innerWidth - clientX) / innerWidth * 100;
  return calcVW;
}

interface RightSidebarResizeControlProps extends BoxProps {
  onResizeSidebar: (size: number) => void;
  isRightSidebarOpen: boolean;
  rightSidebarWidth: number;
}

export const RightSidebarResizeControl = (props: RightSidebarResizeControlProps) => {
  const { onResizeSidebar, isRightSidebarOpen, rightSidebarWidth, ...rest } = props;
  const [isDragging, setIsDragging] = React.useState(false);
  const { unsavedRightSidebarWidth, setUnsavedRightSidebarWidth,
    setIsResizingLayout
  } = React.useContext(LayoutContext);
  const updateWidthTimer = React.useRef<number>();

  const localSidebarWidth = unsavedRightSidebarWidth || rightSidebarWidth;

  const updateWidth = (e) => {
    if (isDragging) {
      setIsResizingLayout(true);
      e.preventDefault();
      const vw = getVW(e, window);
      const clampedVW = clamp(vw, MIN_VW, MAX_VW);
      setUnsavedRightSidebarWidth(clampedVW);

      if (updateWidthTimer.current) {
        clearTimeout(updateWidthTimer.current);
      }
      updateWidthTimer.current = window.setTimeout(() => {
        onResizeSidebar(clampedVW);
        setUnsavedRightSidebarWidth(clampedVW);
        setIsResizingLayout(false);
      }, 1000);
    }
  }

  const moveHandler = (e) => {
    updateWidth(e)
  }

  const mouseUpHandler = () => {
    setIsDragging(false);
  }

  React.useEffect(() => {
    window.addEventListener('mousemove', moveHandler);
    window.addEventListener('mouseup', mouseUpHandler);
    return () => {
      window.removeEventListener('mousemove', moveHandler);
      window.removeEventListener('mouseup', mouseUpHandler);
    }
  });

  if (!isRightSidebarOpen) {
    return null;
  }

  return (
    <Box
      as="button"
      width="3px"
      transform="translateX(50%)"
      position="fixed"
      zIndex={100}
      opacity={0}
      right={localSidebarWidth + "vw"}
      height="100%"
      cursor="col-resize"
      onMouseDown={() => setIsDragging(true)}
      onMouseMove={moveHandler}
      onMouseUp={mouseUpHandler}
      bg="link"
      transition="opacity 0.2s ease-in-out"
      _hover={{ opacity: 1 }}
      {...isDragging && { opacity: 1 }}
      {...rest}
    >
    </Box>
  );
}
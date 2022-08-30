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
}

export const RightSidebarResizeControl = (props: RightSidebarResizeControlProps) => {
  const { onResizeSidebar, ...rest } = props;
  const [isDragging, setIsDragging] = React.useState(false);
  const { unsavedRightSidebarWidth, setUnsavedRightSidebarWidth,
    setIsResizingLayout
  } = React.useContext(LayoutContext);

  const updateWidthTimer = React.useRef<number>();

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
        onResizeSidebar(unsavedRightSidebarWidth);
        setIsResizingLayout(false);
      }, 1000)
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

  return (
    <Box
      as="button"
      width="3px"
      zIndex={100}
      opacity={0}
      position="absolute"
      left={0}
      top={0}
      bottom={0}
      cursor="col-resize"
      onMouseDown={() => setIsDragging(true)}
      onMouseMove={moveHandler}
      onMouseUp={mouseUpHandler}
      bg="link"
      transition="opacity 0.2s ease-in-out"
      _hover={{ opacity: 0.6 }}
      {...isDragging && { opacity: 1 }}
      {...rest}
    >
    </Box>
  );
}
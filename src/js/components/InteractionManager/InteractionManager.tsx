
import React from 'react';
import { Box, ButtonProps } from '@chakra-ui/react';
import { Preview } from './Preview';
import { Actions } from './Actions';
import {
  attrIf,
  updatePreview,
  updateActions,
  updateBlockHover,
  getIsTargetAutolink,
  getIsTargetUrlLink,
  getIsTargetPageLink,
  getIsTargetHashtag
} from './utils/utils';

interface InteractionManagerProps {
  children: React.ReactNode;
  shouldSetBlockIsHovered: boolean;
  shouldShowPreviews: boolean;
  shouldShowActions: boolean;
  actions: ButtonProps[];
  previewEl: React.ReactNode;
  setPreview: (value: any, type: string) => void;
  onNavigateUid: (e: any, uid: string) => void;
  onNavigatePage: (e: any, title: string) => void;
}

export const InteractionManager = ({
  children,
  shouldSetBlockIsHovered,
  shouldShowActions,
  shouldShowPreviews,
  actions,
  previewEl,
  setPreview,
  onNavigateUid,
  onNavigatePage,
}: InteractionManagerProps): React.ReactElement => {
  const [target, setTarget] = React.useState(null);
  const [lastE, setLastE] = React.useState(null);
  const [isScrolling, setIsScrolling] = React.useState(false);
  const [previewPos, setPreviewPos] = React.useState(null);
  const [isUsingActions, setIsUsingActions] = React.useState(false);
  const [actionsPos, setActionsPos] = React.useState(null);
  const [actionsUid, setActionsUid] = React.useState(null);

  // Clear the actions and reset actions data
  const clearActions = (): void => {
    setActionsPos(null);
    setActionsUid(null);
    setIsUsingActions(false);
  }

  // When hovering a page, link, breadcrumb, etc.
  const handleMouseMove = React.useCallback((e) => {

    // if the hovered event looks the same as the last event, do nothing
    if (e.target && (e?.target === lastE?.target)) {
      setLastE(e);
      return
    };

    shouldSetBlockIsHovered && updateBlockHover(e);
    shouldShowActions && updateActions({ e, setActionsPos, isUsingActions, setActionsUid, clearActions });
    shouldShowPreviews && updatePreview({ e, setPreviewPos, setPreview });

    setLastE(e);
    return;
  }, [target, shouldShowPreviews, shouldSetBlockIsHovered, shouldShowActions]);


  // When clicking a page, link, breadcrumb, etc.
  const handleClick = (e) => {

    const clickedPage = attrIf(e, getIsTargetPageLink, 'data-page-title');
    if (clickedPage) {
      onNavigatePage(e, clickedPage);
      return;
    }

    const clickedHashtag = attrIf(e, getIsTargetHashtag, 'data-page-title');
    if (clickedHashtag) {
      onNavigatePage(e, clickedHashtag);
      return;
    }

    // Anchor links
    const clickedUrlLink = attrIf(e, getIsTargetUrlLink, 'href');
    if (clickedUrlLink) {
      e.preventDefault();
      e.stopPropagation();
      window.history.pushState({}, '', clickedUrlLink);
      return;
    }

    const clickedAutoLink = attrIf(e, getIsTargetAutolink, 'href');
    if (clickedAutoLink) {
      e.preventDefault();
      e.stopPropagation();
      window.history.pushState({}, '', clickedAutoLink);
      return;
    }
  }

  return (
    <Box
      display="contents"
      onClick={handleClick}
      onMouseMove={(e) => {
        // update the target in state so we
        // can use it to memoize the mousemove event
        if (e.target !== target) { setTarget(e.target) };
        // reset scroll state in case you're
        // moving fast and you don't want to
        // wait for it to reset
        if (isScrolling) setIsScrolling(false);
        // then handle the move events
        handleMouseMove(e);
      }}
      // Create a scrolling state so we can
      // hide the actions and preview while
      // scrolling. It would be nice to avoid this,
      // but the positioning of the overlays
      // is static, so they'd be floating in space
      // without this.
      onWheel={() => {
        if (!isScrolling) {
          setIsScrolling(true)
          setTimeout(() => {
            setIsScrolling(false)
          }, 1500)
        }
      }}
    >
      {children}
      <Preview
        isOpen={!!previewEl && !isScrolling}
        pos={previewPos}
      >
        {previewEl}
      </Preview>
      <Actions
        actions={actions}
        pos={actionsPos}
        clearActions={clearActions}
        isUsingActions={isUsingActions}
        setIsUsingActions={setIsUsingActions}
        actionsUid={actionsUid}
      />
    </Box>)
}


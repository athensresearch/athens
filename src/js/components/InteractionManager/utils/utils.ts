import React from "react";

// Helpers for the InteractionManager
export const getClosestBlock = (e: React.MouseEvent<HTMLElement>): Element | false => {
  const target = e.target as HTMLElement;
  return target.closest('.block-container')
};
export const getTargetIfInBlockActions = (e: React.MouseEvent<HTMLElement>): Element | false => {
  const target = e.target as HTMLElement;
  return target.closest('block-actions')
};

export const getIsTargetPageLink = (e: React.MouseEvent<HTMLElement>): boolean => {
  const target = e.target as HTMLElement;
  return target.classList?.contains('link')
};
export const getIsTargetUrlLink = (e: React.MouseEvent<HTMLElement>): boolean => {
  const target = e.target as HTMLElement;
  return target.classList?.contains('url-link')
};
export const getIsTargetHashtag = (e: React.MouseEvent<HTMLElement>): boolean => {
  const target = e.target as HTMLElement;
  return target.classList?.contains('hashtag')
};
export const getIsTargetAutolink = (e: React.MouseEvent<HTMLElement>): boolean => {
  const target = e.target as HTMLElement;
  return target.classList?.contains('autolink')
};
export const getIsTargetBlockRef = (e: React.MouseEvent<HTMLElement>): boolean => {
  const target = e.target as HTMLElement;
  return target.classList?.contains('block-ref')
};

interface Coords {
  x: number;
  y: number;
}

/* 
  Returns an attibute of the event target if the
  target meets a provided condition.
  Returns false if the target does not meet the condition.
  Returns the attribute if the target meets the condition.
*/
export const attrIf = (
  e: React.MouseEvent<HTMLElement>,
  condition: (e: React.MouseEvent<HTMLElement>) => boolean,
  attr: string
): string | false => {
  if ((e.target) && condition(e)) {
    const target = e.target as HTMLElement;
    return target.getAttribute(attr)
  };
  return false;
};

const posOfTargetBottomStart = (e: React.MouseEvent<HTMLElement>): Coords => {
  const target = e.target as HTMLElement;
  const rect = target.getBoundingClientRect() as DOMRect;
  return { x: rect.left, y: rect.top + rect.height };
};

interface UpdatePreviewProps {
  e: React.MouseEvent<HTMLElement>;
  setPreviewPos: (pos: Coords) => void;
  setPreview: (value: string, type: string) => void;
}

export const updatePreview = ({ e, setPreviewPos, setPreview }: UpdatePreviewProps): void => {

  const previewedPage = attrIf(e, getIsTargetPageLink, 'data-page-title');
  if (previewedPage) {
    setPreview(previewedPage, 'page');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedHashtag = attrIf(e, getIsTargetHashtag, 'data-page-title');
  if (previewedHashtag) {
    setPreview(previewedHashtag, 'page');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedBlockRef = attrIf(e, getIsTargetBlockRef, 'data-uid');
  if (previewedBlockRef) {
    setPreview(previewedBlockRef, 'block');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedUrl = attrIf(e, getIsTargetUrlLink, 'href');
  if (previewedUrl) {
    setPreview(previewedUrl, 'url');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedAutoUrl = attrIf(e, getIsTargetAutolink, 'href');
  if (previewedAutoUrl) {
    setPreview(previewedAutoUrl, 'url');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  setPreview(null, null);
}

interface SetActionsOnBlockProps {
  block: HTMLElement;
  setActionsPos: (Coords) => void;
  clearActions: () => void;
  setActionsUid: (uid: string) => void;
}

const setActionsOnBlock = ({ block, setActionsPos, setActionsUid, clearActions }: SetActionsOnBlockProps): void => {
  // if we have a block...
  if (block) {
    // get the bounding box
    const blockBox = block.getBoundingClientRect() as DOMRect;
    // update the position to the top-right corner of the block
    setActionsPos({ x: blockBox.left + blockBox.width, y: blockBox.top });
    setActionsUid(block.getAttribute('data-uid'));
    return;
  } else {
    // if we don't have a block, reset actions and position
    clearActions();
    return;
  }
}

interface UpdateActionsProps {
  e: React.MouseEvent<HTMLElement>;
  setActionsPos: (Coords) => void;
  isUsingActions: boolean;
  setActionsUid: (uid: string | false) => void;
  clearActions: () => void;
}

// Determine whether to show block actions
// based on mousemove event target
export const updateActions = ({ e, setActionsPos, isUsingActions, setActionsUid, clearActions }: UpdateActionsProps): void => {

  // If we're currently using actions, we shouldn't update actions
  if (isUsingActions) return;

  // get details about the hovered element
  const closestBlock = getClosestBlock(e) as HTMLElement;

  // if we have a block and we're not already using actions
  if (closestBlock) {
    setActionsOnBlock({ block: closestBlock, setActionsPos, setActionsUid, clearActions });
    setActionsUid(closestBlock.getAttribute('data-uid'));
    return;

    // otherwise, clear actions
  } else {
    clearActions();
    return;
  }
}

// If I'm hovering anywhere within a block, set a class on that block
export const updateBlockHover = (e: React.MouseEvent<HTMLElement>): void => {

  // get the closest block
  const targetBlock = getClosestBlock(e);

  if (targetBlock) {
    // if we're hovering over a block, add the hover class
    targetBlock.classList.add('is-hovered');
    // and remove the class when the mouse leaves the block
    targetBlock.addEventListener('mouseout', () => targetBlock.classList.remove('is-hovered'));
  }
}
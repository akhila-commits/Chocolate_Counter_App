package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CacaoBlack
import com.example.ui.theme.CacaoBorder
import com.example.ui.theme.CacaoCard
import com.example.ui.theme.CacaoDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MarkTechBlue
import com.example.ui.theme.MarkTechCyan
import com.example.ui.theme.MarkTechGrey
import com.example.ui.theme.MarkTechNavy
import com.example.ui.theme.MarkTechNavyDark

/**
 * Geometric MT Monogram Logo representing MARK-TECH.
 * M: Pure White / Light Slate faceted mark.
 * T: Electric Blue dynamic overlapping slant.
 */
@Composable
fun MarkTechLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    showBackground: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBackground) {
                    Modifier
                        .background(MarkTechNavyDark, RoundedCornerShape(6.dp))
                        .border(1.dp, MarkTechBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(size * 0.12f)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // --- Letter 'M' (Faceted White / Light Grey) ---
            // Left vertical column of M
            val leftCol = Path().apply {
                moveTo(w * 0.14f, h * 0.36f)
                lineTo(w * 0.28f, h * 0.28f)
                lineTo(w * 0.28f, h * 0.78f)
                lineTo(w * 0.14f, h * 0.78f)
                close()
            }
            drawPath(path = leftCol, color = Color.White)

            // Inner downward slope of M
            val innerSlopeDown = Path().apply {
                moveTo(w * 0.28f, h * 0.28f)
                lineTo(w * 0.44f, h * 0.48f)
                lineTo(w * 0.44f, h * 0.62f)
                lineTo(w * 0.28f, h * 0.40f)
                close()
            }
            drawPath(path = innerSlopeDown, color = Color(0xFFCBD5E1))

            // Inner right column of M
            val rightCol = Path().apply {
                moveTo(w * 0.44f, h * 0.48f)
                lineTo(w * 0.56f, h * 0.34f)
                lineTo(w * 0.56f, h * 0.78f)
                lineTo(w * 0.44f, h * 0.78f)
                close()
            }
            drawPath(path = rightCol, color = Color(0xFFF1F5F9))

            // --- Letter 'T' (Electric Blue overlapping crossbar and stem) ---
            // Slanted crossbar of T (overlapping M)
            val topBarT = Path().apply {
                moveTo(w * 0.36f, h * 0.36f)
                lineTo(w * 0.68f, h * 0.18f)
                lineTo(w * 0.84f, h * 0.18f)
                lineTo(w * 0.48f, h * 0.42f)
                close()
            }
            drawPath(
                path = topBarT,
                brush = Brush.linearGradient(
                    colors = listOf(MarkTechCyan, MarkTechBlue),
                    start = Offset(w * 0.36f, h * 0.36f),
                    end = Offset(w * 0.84f, h * 0.18f)
                )
            )

            // Vertical stem of T
            val stemT = Path().apply {
                moveTo(w * 0.65f, h * 0.30f)
                lineTo(w * 0.80f, h * 0.22f)
                lineTo(w * 0.80f, h * 0.78f)
                lineTo(w * 0.65f, h * 0.78f)
                close()
            }
            drawPath(
                path = stemT,
                brush = Brush.verticalGradient(
                    colors = listOf(MarkTechBlue, Color(0xFF1D4ED8)),
                    startY = h * 0.22f,
                    endY = h * 0.78f
                )
            )
        }
    }
}

/**
 * "Powered by MARK-TECH" interactive badge.
 * Displays the stylized MT mark, "Powered by", "MARK-", and "TECH" in brand colors.
 */
@Composable
fun PoweredByMarkTechBadge(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = true) {
                if (onClick != null) {
                    onClick()
                } else {
                    showDialog = true
                }
            }
            .testTag("powered_by_mark_tech_badge"),
        color = MarkTechNavy.copy(alpha = 0.85f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MarkTechBlue.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 3.dp else 5.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MarkTechLogoMark(
                size = if (compact) 16.dp else 20.dp,
                showBackground = true
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = MarkTechGrey,
                            fontWeight = FontWeight.Normal,
                            fontSize = if (compact) 10.sp else 11.5.sp
                        )
                    ) {
                        append("Powered by ")
                    }
                    withStyle(
                        SpanStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 10.5.sp else 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    ) {
                        append("MARK-")
                    }
                    withStyle(
                        SpanStyle(
                            color = MarkTechBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (compact) 10.5.sp else 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    ) {
                        append("TECH")
                    }
                }
            )

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "About MARK-TECH",
                tint = MarkTechBlue.copy(alpha = 0.7f),
                modifier = Modifier.size(if (compact) 11.dp else 13.dp)
            )
        }
    }

    if (showDialog) {
        MarkTechAboutDialog(onDismiss = { showDialog = false })
    }
}

/**
 * Receipt & Screen Footer for MARK-TECH branding.
 */
@Composable
fun PoweredByMarkTechFooter(
    modifier: Modifier = Modifier,
    showTagline: Boolean = true,
    compact: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MarkTechLogoMark(
                size = if (compact) 16.dp else 20.dp,
                showBackground = true
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = MarkTechGrey,
                            fontWeight = FontWeight.Normal,
                            fontSize = if (compact) 10.sp else 11.5.sp
                        )
                    ) {
                        append("Powered by ")
                    }
                    withStyle(
                        SpanStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 11.sp else 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    ) {
                        append("MARK-")
                    }
                    withStyle(
                        SpanStyle(
                            color = MarkTechBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (compact) 11.sp else 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    ) {
                        append("TECH")
                    }
                }
            )
        }

        if (showTagline) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "WHERE TECHNOLOGY MEETS GROWTH.",
                color = MarkTechGrey.copy(alpha = 0.85f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp
            )
        }
    }

    if (showDialog) {
        MarkTechAboutDialog(onDismiss = { showDialog = false })
    }
}

/**
 * Company info modal dialog detailing MARK-TECH.
 */
@Composable
fun MarkTechAboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge with MT monogram
                Card(
                    modifier = Modifier.size(68.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MarkTechNavyDark),
                    border = BorderStroke(1.5.dp, MarkTechBlue)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(68.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MarkTechLogoMark(size = 46.dp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "MARK-",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "TECH",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MarkTechBlue,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "WHERE TECHNOLOGY MEETS GROWTH.",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MarkTechGrey,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = MarkTechBlue.copy(alpha = 0.3f)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MarkTechNavy.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MarkTechBlue.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MarkTechBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Certified Technology Partner",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This Counter POS & Order Calculator solution for Revello Speciality Outlet is engineered and powered by MARK-TECH.",
                            color = MarkTechGrey,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enterprise POS • Retail Systems • Growth Tech",
                    fontSize = 11.sp,
                    color = Color(0xFFD7CCC8),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MarkTechBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("mark_tech_about_close_btn")
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CacaoCard,
        shape = RoundedCornerShape(16.dp)
    )
}

package com.naufalverse.iclan.managers;

import com.naufalverse.iclan.IClanPlugin;
import com.naufalverse.iclan.objects.Clan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClanManagerTest {

    @Mock
    private IClanPlugin mockPlugin;

    @Mock
    private DataManager mockDataManager;

    private ClanManager clanManager;
    private UUID testPlayerUUID;
    private Clan testClan;

    @BeforeEach
    void setUp() {
        System.out.println("\n🚀 Setting up ClanManager test...");
        MockitoAnnotations.openMocks(this);

        when(mockPlugin.getDataManager()).thenReturn(mockDataManager);

        clanManager = new ClanManager(mockPlugin);
        testPlayerUUID = UUID.randomUUID();
        testClan = new Clan("TestClan", testPlayerUUID);

        System.out.println("✅ Test setup complete - ClanManager initialized");
    }

    @Test
    void testCreateClan() {
        System.out.println("🔧 Testing clan creation...");

        clanManager.createClan(testClan);
        System.out.println("✅ Test clan 'TestClan' created successfully!");

        assertTrue(clanManager.clanExists("TestClan"));
        assertEquals(testClan, clanManager.getClan("TestClan"));
        assertEquals(testClan, clanManager.getPlayerClan(testPlayerUUID));
        assertEquals(1, clanManager.getClanCount());

        System.out.println("✅ Clan creation test passed - Clan exists and player is owner");
        verify(mockDataManager, times(1)).saveData();
    }

    @Test
    void testDeleteClan() {
        System.out.println("🔧 Testing clan deletion...");

        clanManager.createClan(testClan);
        System.out.println("✅ Test clan 'TestClan' created for deletion test");
        assertTrue(clanManager.clanExists("TestClan"));

        clanManager.deleteClan("TestClan");
        System.out.println("🗑️ Test clan 'TestClan' deleted successfully!");

        assertFalse(clanManager.clanExists("TestClan"));
        assertNull(clanManager.getClan("TestClan"));
        assertNull(clanManager.getPlayerClan(testPlayerUUID));
        assertEquals(0, clanManager.getClanCount());

        System.out.println("✅ Clan deletion test passed - Clan no longer exists and player removed");
        verify(mockDataManager, times(2)).saveData();
    }
}